package com.dtp.core.notify;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.dto.NotifyItem;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.common.em.QueueTypeEnum;
import com.dtp.common.em.RejectedTypeEnum;
import com.dtp.core.context.DtpContext;
import com.dtp.core.context.DtpContextHolder;
import com.dtp.core.handler.NotifierHandler;
import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.thread.ThreadPoolBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * AlarmManager related
 *
 * @author: yanhom
 * @since 1.0.0
 */
@Slf4j
public class AlarmManager {

    private static final ExecutorService ALARM_EXECUTOR = ThreadPoolBuilder.newBuilder()
            .threadPoolName("dtp-alarm")
            .threadFactory("dtp-alarm")
            .corePoolSize(2)
            .maximumPoolSize(4)
            .workQueue(QueueTypeEnum.LINKED_BLOCKING_QUEUE.getName(), 2000, false)
            .rejectedExecutionHandler(RejectedTypeEnum.ABORT_POLICY.getName())
            .buildWithTtl();

    private AlarmManager() {}

    public static void triggerAlarm(Runnable runnable) {
        ALARM_EXECUTOR.execute(runnable);
    }

    public static void doAlarm(DtpExecutor executor, List<NotifyTypeEnum> typeEnums) {
        typeEnums.forEach(x -> doAlarm(executor, x));
    }

    public static void doAlarm(DtpExecutor executor, NotifyTypeEnum typeEnum) {

        boolean triggerCondition = false;
        if (typeEnum == NotifyTypeEnum.REJECT) {
            triggerCondition = checkReject(executor);
        } else if (typeEnum == NotifyTypeEnum.CAPACITY) {
            triggerCondition = checkCapacity(executor);
        } else if (typeEnum == NotifyTypeEnum.LIVENESS) {
            triggerCondition = checkLiveness(executor);
        }
        if (!triggerCondition) {
            return;
        }

        boolean ifAlarm = AlarmLimiter.ifAlarm(executor, typeEnum.getValue());
        if (!ifAlarm) {
            log.warn("DynamicTp notify, alarm limit, dtpName: {}, type: {}",
                    executor.getThreadPoolName(), typeEnum.getValue());
            return;
        }
        DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
        NotifyItem notifyItem = NotifyHelper.getNotifyItem(executor, typeEnum);
        DtpContext dtpContext = DtpContext.builder()
                .dtpExecutor(executor)
                .platforms(dtpProperties.getPlatforms())
                .notifyItem(notifyItem)
                .build();
        DtpContextHolder.set(dtpContext);
        AlarmLimiter.putVal(executor, typeEnum.getValue());
        NotifierHandler.getInstance().sendAlarm(typeEnum);
    }

    private static boolean checkLiveness(DtpExecutor executor) {

        NotifyItem notifyItem = NotifyHelper.getNotifyItem(executor, NotifyTypeEnum.LIVENESS);
        if (Objects.isNull(notifyItem)) {
            return false;
        }

        int maximumPoolSize = executor.getMaximumPoolSize();
        double div = NumberUtil.div(executor.getActiveCount(), maximumPoolSize, 2) * 100;
        return satisfyBaseCondition(notifyItem) && div >= notifyItem.getThreshold();
    }

    private static boolean checkCapacity(DtpExecutor executor) {
        BlockingQueue<Runnable> workQueue = executor.getQueue();
        if (CollUtil.isEmpty(workQueue)) {
            return false;
        }

        NotifyItem notifyItem = NotifyHelper.getNotifyItem(executor, NotifyTypeEnum.CAPACITY);
        if (Objects.isNull(notifyItem)) {
            return false;
        }

        int queueCapacity = executor.getQueueCapacity();
        double div = NumberUtil.div(workQueue.size(), queueCapacity, 2) * 100;
        return satisfyBaseCondition(notifyItem) && div >= notifyItem.getThreshold();
    }

    private static boolean checkReject(DtpExecutor executor) {
        NotifyItem notifyItem = NotifyHelper.getNotifyItem(executor, NotifyTypeEnum.REJECT);
        if (Objects.isNull(notifyItem)) {
            return false;
        }

        int rejectCount = executor.getRejectCount();
        return satisfyBaseCondition(notifyItem) && rejectCount >= notifyItem.getThreshold();
    }

    private static boolean satisfyBaseCondition(NotifyItem notifyItem) {
        return notifyItem.isEnabled() && CollUtil.isNotEmpty(notifyItem.getPlatforms());
    }
}
