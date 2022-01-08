package io.lyh.dtp.notify;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import io.lyh.dtp.common.em.NotifyTypeEnum;
import io.lyh.dtp.common.em.QueueTypeEnum;
import io.lyh.dtp.common.em.RejectedTypeEnum;
import io.lyh.dtp.config.DtpProperties;
import io.lyh.dtp.core.DtpContextHolder;
import io.lyh.dtp.core.DtpExecutor;
import io.lyh.dtp.core.ThreadPoolBuilder;
import io.lyh.dtp.core.DtpContext;
import io.lyh.dtp.handler.NotifierHandler;
import io.lyh.dtp.support.ApplicationContextHolder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * AlarmManager related
 *
 * @author: yanhom1314@gmail.com
 * @date 2022-01-02 下午12:06
 */
@Slf4j
public class AlarmManager {

    private static final ExecutorService ALARM_EXECUTOR = ThreadPoolBuilder.newBuilder()
            .threadPoolName("dtp-alarm")
            .threadFactory("tdp-alarm")
            .dynamic(false)
            .corePoolSize(4)
            .workQueue(QueueTypeEnum.LINKED_BLOCKING_QUEUE.getName(), 5000, false)
            .rejectedExecutionHandler(RejectedTypeEnum.ABORT_POLICY.getName())
            .buildWrapperWithTtl();

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
        return notifyItem.isEnabled() && div >= notifyItem.getThreshold();
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
        return notifyItem.isEnabled() && div >= notifyItem.getThreshold();
    }

    private static boolean checkReject(DtpExecutor executor) {
        NotifyItem notifyItem = NotifyHelper.getNotifyItem(executor, NotifyTypeEnum.REJECT);
        if (Objects.isNull(notifyItem)) {
            return false;
        }

        int rejectCount = executor.getRejectCount();
        return notifyItem.isEnabled() && rejectCount >= notifyItem.getThreshold();
    }

}
