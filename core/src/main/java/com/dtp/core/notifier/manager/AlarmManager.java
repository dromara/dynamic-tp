package com.dtp.core.notifier.manager;

import cn.hutool.core.util.NumberUtil;
import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.em.RejectedTypeEnum;
import com.dtp.common.entity.AlarmInfo;
import com.dtp.common.entity.NotifyItem;
import com.dtp.common.pattern.filter.InvokerChain;
import com.dtp.core.context.AlarmCtx;
import com.dtp.core.context.BaseNotifyCtx;
import com.dtp.core.notifier.alarm.AlarmCounter;
import com.dtp.core.notifier.alarm.AlarmLimiter;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.support.ThreadPoolBuilder;
import com.dtp.core.support.runnable.DtpRunnable;
import com.dtp.core.support.wrapper.TaskWrappers;
import com.dtp.core.thread.DtpExecutor;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static com.dtp.common.constant.DynamicTpConst.TRACE_ID;
import static com.dtp.common.em.QueueTypeEnum.LINKED_BLOCKING_QUEUE;

/**
 * AlarmManager related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public class AlarmManager {

    private static final ExecutorService ALARM_EXECUTOR = ThreadPoolBuilder.newBuilder()
            .threadFactory("dtp-alarm")
            .corePoolSize(1)
            .maximumPoolSize(2)
            .workQueue(LINKED_BLOCKING_QUEUE.getName(), 2000)
            .rejectedExecutionHandler(RejectedTypeEnum.DISCARD_OLDEST_POLICY.getName())
            .rejectEnhanced(false)
            .taskWrappers(TaskWrappers.getInstance().getByNames(Sets.newHashSet("mdc")))
            .buildDynamic();

    private static final InvokerChain<BaseNotifyCtx> ALARM_INVOKER_CHAIN;

    static {
        ALARM_INVOKER_CHAIN = NotifyFilterBuilder.getAlarmInvokerChain();
        Runtime.getRuntime().addShutdownHook(new Thread(ALARM_EXECUTOR::shutdown));
    }

    private AlarmManager() { }

    public static void initAlarm(String poolName, List<NotifyItem> notifyItems) {
        notifyItems.forEach(x -> initAlarm(poolName, x));
    }

    public static void initAlarm(String poolName, NotifyItem notifyItem) {
        AlarmLimiter.initAlarmLimiter(poolName, notifyItem);
        AlarmCounter.init(poolName, notifyItem.getType());
    }

    public static void doAlarmAsync(DtpExecutor executor, NotifyItemEnum notifyType) {
        AlarmCounter.incAlarmCounter(executor.getThreadPoolName(), notifyType.getValue());
        ALARM_EXECUTOR.execute(() -> doAlarm(ExecutorWrapper.of(executor), notifyType));
    }

    public static void doAlarmAsync(DtpExecutor executor, NotifyItemEnum notifyType, Runnable currRunnable) {
        MDC.put(TRACE_ID, ((DtpRunnable) currRunnable).getTraceId());
        AlarmCounter.incAlarmCounter(executor.getThreadPoolName(), notifyType.getValue());
        ALARM_EXECUTOR.execute(() -> doAlarm(ExecutorWrapper.of(executor), notifyType));
    }

    public static void doAlarmAsync(DtpExecutor executor, List<NotifyItemEnum> notifyItemEnums) {
        doAlarmAsync(ExecutorWrapper.of(executor), notifyItemEnums);
    }

    public static void doAlarmAsync(ExecutorWrapper executorWrapper, List<NotifyItemEnum> notifyItemEnums) {
        ALARM_EXECUTOR.execute(() -> notifyItemEnums.forEach(x -> doAlarm(executorWrapper, x)));
    }

    public static void doAlarm(ExecutorWrapper executorWrapper, NotifyItemEnum notifyItemEnum) {
        NotifyHelper.getNotifyItem(executorWrapper, notifyItemEnum).ifPresent(notifyItem -> {
            val alarmCtx = new AlarmCtx(executorWrapper, notifyItem);
            ALARM_INVOKER_CHAIN.proceed(alarmCtx);
        });
    }

    public static boolean checkThreshold(ExecutorWrapper executor, NotifyItemEnum itemEnum, NotifyItem notifyItem) {

        switch (itemEnum) {
            case CAPACITY:
                return checkCapacity(executor, notifyItem);
            case LIVENESS:
                return checkLiveness(executor, notifyItem);
            case REJECT:
            case RUN_TIMEOUT:
            case QUEUE_TIMEOUT:
                return checkWithAlarmInfo(executor, notifyItem);
            default:
                log.error("Unsupported alarm type, type: {}", itemEnum);
                return false;
        }
    }

    private static boolean checkLiveness(ExecutorWrapper executorWrapper, NotifyItem notifyItem) {
        val executor = executorWrapper.getExecutor();
        int maximumPoolSize = executor.getMaximumPoolSize();
        double div = NumberUtil.div(executor.getActiveCount(), maximumPoolSize, 2) * 100;
        return div >= notifyItem.getThreshold();
    }

    private static boolean checkCapacity(ExecutorWrapper executorWrapper, NotifyItem notifyItem) {

        val executor = executorWrapper.getExecutor();
        if (CollectionUtils.isEmpty(executor.getQueue())) {
            return false;
        }
        double div = NumberUtil.div(executor.getQueueSize(), executor.getQueueCapacity(), 2) * 100;
        return div >= notifyItem.getThreshold();
    }

    private static boolean checkWithAlarmInfo(ExecutorWrapper executorWrapper, NotifyItem notifyItem) {
        AlarmInfo alarmInfo = AlarmCounter.getAlarmInfo(executorWrapper.getThreadPoolName(), notifyItem.getType());
        return alarmInfo.getCount() >= notifyItem.getThreshold();
    }
}
