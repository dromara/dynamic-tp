package com.dtp.core.notify;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.dto.AlarmInfo;
import com.dtp.common.dto.ExecutorWrapper;
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
import lombok.val;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

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
        val executorWrapper = new ExecutorWrapper(executor.getThreadPoolName(), executor, executor.getNotifyItems());
        doAlarm(executorWrapper, typeEnums);
    }

    public static void doAlarm(ExecutorWrapper executorWrapper, List<NotifyTypeEnum> typeEnums) {
        typeEnums.forEach(x -> doAlarm(executorWrapper, x));
    }

    public static void doAlarm(DtpExecutor executor, NotifyTypeEnum typeEnum) {
        val executorWrapper = new ExecutorWrapper(executor.getThreadPoolName(), executor, executor.getNotifyItems());
        doAlarm(executorWrapper, typeEnum);
    }

    public static void doAlarm(ExecutorWrapper executorWrapper, NotifyTypeEnum typeEnum) {
        if (!preCheck(executorWrapper, typeEnum)) {
            return;
        }
        boolean ifAlarm = AlarmLimiter.ifAlarm(executorWrapper.getThreadPoolName(), typeEnum.getValue());
        if (!ifAlarm) {
            log.debug("DynamicTp notify, alarm limit, dtpName: {}, type: {}",
                    executorWrapper.getThreadPoolName(), typeEnum.getValue());
            return;
        }
        DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
        NotifyItem notifyItem = NotifyHelper.getNotifyItem(executorWrapper, typeEnum);
        if (Objects.isNull(notifyItem)) {
            return;
        }

        synchronized (SEND_LOCK) {
            // recheck alarm limit.
            ifAlarm = AlarmLimiter.ifAlarm(executorWrapper.getThreadPoolName(), typeEnum.getValue());
            if (!ifAlarm) {
                log.warn("DynamicTp notify, concurrent send, alarm limit, dtpName: {}, type: {}",
                        executorWrapper.getThreadPoolName(), typeEnum.getValue());
                return;
            }
            AlarmLimiter.putVal(executorWrapper.getThreadPoolName(), typeEnum.getValue());
        }

        AlarmInfo alarmInfo = AlarmCounter.getAlarmInfo(executorWrapper.getThreadPoolName(), notifyItem.getType());
        DtpContext dtpContext = DtpContext.builder()
                .executorWrapper(executorWrapper)
                .platforms(dtpProperties.getPlatforms())
                .notifyItem(notifyItem)
                .alarmInfo(alarmInfo)
                .build();
        DtpContextHolder.set(dtpContext);
        NotifierHandler.getInstance().sendAlarm(typeEnum);
        AlarmCounter.reset(executorWrapper.getThreadPoolName(), notifyItem.getType());
    }

    private static boolean preCheck(ExecutorWrapper executorWrapper, NotifyTypeEnum typeEnum) {
        switch (typeEnum) {
            case REJECT:
                return checkReject(executorWrapper);
            case CAPACITY:
                return checkCapacity(executorWrapper);
            case LIVENESS:
                return checkLiveness(executorWrapper);
            case RUN_TIMEOUT:
                return checkRunTimeout(executorWrapper);
            case QUEUE_TIMEOUT:
                return checkQueueTimeout(executorWrapper);
            default:
                log.error("Unsupported alarm type, type: {}", typeEnum);
                return false;
        }
    }

    private static boolean checkLiveness(ExecutorWrapper executorWrapper) {

        NotifyItem notifyItem = NotifyHelper.getNotifyItem(executorWrapper, NotifyTypeEnum.LIVENESS);
        if (Objects.isNull(notifyItem)) {
            return false;
        }

        val executor = (ThreadPoolExecutor) executorWrapper.getExecutor();
        int maximumPoolSize = executor.getMaximumPoolSize();
        double div = NumberUtil.div(executor.getActiveCount(), maximumPoolSize, 2) * 100;
        return satisfyBaseCondition(notifyItem) && div >= notifyItem.getThreshold();
    }

    private static boolean checkCapacity(ExecutorWrapper executorWrapper) {

        val executor = (ThreadPoolExecutor) executorWrapper.getExecutor();
        BlockingQueue<Runnable> workQueue = executor.getQueue();
        if (CollUtil.isEmpty(workQueue)) {
            return false;
        }

        NotifyItem notifyItem = NotifyHelper.getNotifyItem(executorWrapper, NotifyTypeEnum.CAPACITY);
        if (Objects.isNull(notifyItem)) {
            return false;
        }

        int queueCapacity = executor.getQueue().size() + executor.getQueue().remainingCapacity();
        double div = NumberUtil.div(workQueue.size(), queueCapacity, 2) * 100;
        return satisfyBaseCondition(notifyItem) && div >= notifyItem.getThreshold();
    }

    private static boolean checkReject(ExecutorWrapper executorWrapper) {
        NotifyItem notifyItem = NotifyHelper.getNotifyItem(executorWrapper, NotifyTypeEnum.REJECT);
        if (Objects.isNull(notifyItem)) {
            return false;
        }

        AlarmInfo alarmInfo = AlarmCounter.getAlarmInfo(executorWrapper.getThreadPoolName(), notifyItem.getType());
        int rejectCount = alarmInfo.getCount();
        return satisfyBaseCondition(notifyItem) && rejectCount >= notifyItem.getThreshold();
    }

    private static boolean checkRunTimeout(ExecutorWrapper executorWrapper) {
        NotifyItem notifyItem = NotifyHelper.getNotifyItem(executorWrapper, NotifyTypeEnum.RUN_TIMEOUT);
        if (Objects.isNull(notifyItem)) {
            return false;
        }

        AlarmInfo alarmInfo = AlarmCounter.getAlarmInfo(executorWrapper.getThreadPoolName(), notifyItem.getType());
        int runTimeoutTaskCount = alarmInfo.getCount();
        return satisfyBaseCondition(notifyItem) && runTimeoutTaskCount >= notifyItem.getThreshold();
    }

    private static boolean checkQueueTimeout(ExecutorWrapper executorWrapper) {
        NotifyItem notifyItem = NotifyHelper.getNotifyItem(executorWrapper, NotifyTypeEnum.QUEUE_TIMEOUT);
        if (Objects.isNull(notifyItem)) {
            return false;
        }

        AlarmInfo alarmInfo = AlarmCounter.getAlarmInfo(executorWrapper.getThreadPoolName(), notifyItem.getType());
        int queueTimeoutTaskCount = alarmInfo.getCount();
        return satisfyBaseCondition(notifyItem) && queueTimeoutTaskCount >= notifyItem.getThreshold();
    }

    private static boolean satisfyBaseCondition(NotifyItem notifyItem) {
        return notifyItem.isEnabled() && CollUtil.isNotEmpty(notifyItem.getPlatforms());
    }
}
