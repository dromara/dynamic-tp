package com.dtp.core.notify.manager;

import cn.hutool.core.util.NumberUtil;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.constant.DynamicTpConst;
import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.entity.AlarmInfo;
import com.dtp.common.entity.NotifyItem;
import com.dtp.common.pattern.filter.InvokerChain;
import com.dtp.core.context.AlarmCtx;
import com.dtp.core.context.BaseNotifyCtx;
import com.dtp.core.notify.alarm.AlarmCounter;
import com.dtp.core.notify.alarm.AlarmLimiter;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.support.runnable.DtpRunnable;
import com.dtp.core.thread.DtpExecutor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import static com.dtp.common.constant.DynamicTpConst.TRACE_ID;

/**
 * AlarmManager related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public class AlarmManager {

    private static final InvokerChain<BaseNotifyCtx> ALARM_INVOKER_CHAIN;

    static {
        ALARM_INVOKER_CHAIN = NotifyFilterBuilder.getAlarmInvokerChain();
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
        ApplicationContextHolder.getBean(DynamicTpConst.ALARM_NAME, ExecutorService.class)
                .execute(() -> doAlarm(ExecutorWrapper.of(executor), notifyType));
    }

    public static void doAlarmAsync(DtpExecutor executor, NotifyItemEnum notifyType, Runnable currRunnable) {
        MDC.put(TRACE_ID, ((DtpRunnable) currRunnable).getTraceId());
        AlarmCounter.incAlarmCounter(executor.getThreadPoolName(), notifyType.getValue());
        ApplicationContextHolder.getBean(DynamicTpConst.ALARM_NAME, ExecutorService.class)
                .execute(() -> doAlarm(ExecutorWrapper.of(executor), notifyType));
    }

    public static void doAlarmAsync(DtpExecutor executor, List<NotifyItemEnum> notifyItemEnums) {
        doAlarmAsync(ExecutorWrapper.of(executor), notifyItemEnums);
    }

    public static void doAlarmAsync(ExecutorWrapper executorWrapper, List<NotifyItemEnum> notifyItemEnums) {
        ApplicationContextHolder.getBean(DynamicTpConst.ALARM_NAME, ExecutorService.class)
                .execute(() -> notifyItemEnums.forEach(x -> doAlarm(executorWrapper, x)));
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
        BlockingQueue<Runnable> workQueue = executor.getQueue();
        if (CollectionUtils.isEmpty(workQueue)) {
            return false;
        }

        int queueCapacity = executor.getQueue().size() + executor.getQueue().remainingCapacity();
        double div = NumberUtil.div(workQueue.size(), queueCapacity, 2) * 100;
        return div >= notifyItem.getThreshold();
    }

    private static boolean checkWithAlarmInfo(ExecutorWrapper executorWrapper, NotifyItem notifyItem) {
        AlarmInfo alarmInfo = AlarmCounter.getAlarmInfo(executorWrapper.getThreadPoolName(), notifyItem.getType());
        return alarmInfo.getCount() >= notifyItem.getThreshold();
    }
}
