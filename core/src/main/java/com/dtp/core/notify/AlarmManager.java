package com.dtp.core.notify;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.dto.AlarmInfo;
import com.dtp.common.dto.NotifyItem;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.common.em.QueueTypeEnum;
import com.dtp.common.em.RejectedTypeEnum;
import com.dtp.core.context.DtpContext;
import com.dtp.core.context.DtpContextHolder;
import com.dtp.core.handler.NotifierHandler;
import com.dtp.core.support.ThreadPoolBuilder;
import com.dtp.core.thread.DtpExecutor;
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
            .rejectedExecutionHandler(RejectedTypeEnum.DISCARD_OLDEST_POLICY.getName())
            .dynamic(false)
            .buildWithTtl();

    private static final Object SEND_LOCK = new Object();

    private AlarmManager() {}

    public static void triggerAlarm(String dtpName, String notifyType, Runnable runnable) {
        AlarmCounter.incAlarmCounter(dtpName, notifyType);
        ALARM_EXECUTOR.execute(runnable);
    }

    public static void triggerAlarm(Runnable runnable) {
        ALARM_EXECUTOR.execute(runnable);
    }

    public static void doAlarm(DtpExecutor executor, List<NotifyTypeEnum> typeEnums) {
        typeEnums.forEach(x -> doAlarm(executor, x));
    }

    public static void doAlarm(DtpExecutor executor, NotifyTypeEnum typeEnum) {
        if (!preCheck(executor, typeEnum)) {
            return;
        }
        boolean ifAlarm = AlarmLimiter.ifAlarm(executor.getThreadPoolName(), typeEnum.getValue());
        if (!ifAlarm) {
            log.debug("DynamicTp notify, alarm limit, dtpName: {}, type: {}",
                    executor.getThreadPoolName(), typeEnum.getValue());
            return;
        }
        DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
        NotifyItem notifyItem = NotifyHelper.getNotifyItem(executor, typeEnum);
        if (Objects.isNull(notifyItem)) {
            return;
        }

        synchronized (SEND_LOCK) {
            // recheck alarm limit.
            ifAlarm = AlarmLimiter.ifAlarm(executor.getThreadPoolName(), typeEnum.getValue());
            if (!ifAlarm) {
                log.warn("DynamicTp notify, concurrent send, alarm limit, dtpName: {}, type: {}",
                        executor.getThreadPoolName(), typeEnum.getValue());
                return;
            }
            AlarmLimiter.putVal(executor.getThreadPoolName(), typeEnum.getValue());
        }

        AlarmInfo alarmInfo = AlarmCounter.getAlarmInfo(executor.getThreadPoolName(), notifyItem.getType());
        DtpContext dtpContext = DtpContext.builder()
                .dtpExecutor(executor)
                .platforms(dtpProperties.getPlatforms())
                .notifyItem(notifyItem)
                .alarmInfo(alarmInfo)
                .build();
        DtpContextHolder.set(dtpContext);
        NotifierHandler.getInstance().sendAlarm(typeEnum);
        AlarmCounter.reset(executor.getThreadPoolName(), notifyItem.getType());
    }

    private static boolean preCheck(DtpExecutor executor, NotifyTypeEnum typeEnum) {
        switch (typeEnum) {
            case REJECT:
                return checkReject(executor);
            case CAPACITY:
                return checkCapacity(executor);
            case LIVENESS:
                return checkLiveness(executor);
            case RUN_TIMEOUT:
                return checkRunTimeout(executor);
            case QUEUE_TIMEOUT:
                return checkQueueTimeout(executor);
            default:
                log.error("Unsupported alarm type, type: {}", typeEnum);
                return false;
        }
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

        AlarmInfo alarmInfo = AlarmCounter.getAlarmInfo(executor.getThreadPoolName(), notifyItem.getType());
        int rejectCount = alarmInfo.getCount();
        return satisfyBaseCondition(notifyItem) && rejectCount >= notifyItem.getThreshold();
    }

    private static boolean checkRunTimeout(DtpExecutor executor) {
        NotifyItem notifyItem = NotifyHelper.getNotifyItem(executor, NotifyTypeEnum.RUN_TIMEOUT);
        if (Objects.isNull(notifyItem)) {
            return false;
        }

        AlarmInfo alarmInfo = AlarmCounter.getAlarmInfo(executor.getThreadPoolName(), notifyItem.getType());
        int runTimeoutTaskCount = alarmInfo.getCount();
        return satisfyBaseCondition(notifyItem) && runTimeoutTaskCount >= notifyItem.getThreshold();
    }

    private static boolean checkQueueTimeout(DtpExecutor executor) {
        NotifyItem notifyItem = NotifyHelper.getNotifyItem(executor, NotifyTypeEnum.QUEUE_TIMEOUT);
        if (Objects.isNull(notifyItem)) {
            return false;
        }

        AlarmInfo alarmInfo = AlarmCounter.getAlarmInfo(executor.getThreadPoolName(), notifyItem.getType());
        int queueTimeoutTaskCount = alarmInfo.getCount();
        return satisfyBaseCondition(notifyItem) && queueTimeoutTaskCount >= notifyItem.getThreshold();
    }

    private static boolean satisfyBaseCondition(NotifyItem notifyItem) {
        return notifyItem.isEnabled() && CollUtil.isNotEmpty(notifyItem.getPlatforms());
    }
}
