package com.dtp.core.notify.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.dto.AlarmInfo;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.dto.NotifyItem;
import com.dtp.common.dto.NotifyPlatform;
import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.em.RejectedTypeEnum;
import com.dtp.common.pattern.filter.InvokerChain;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.util.StreamUtil;
import com.dtp.core.context.AlarmCtx;
import com.dtp.core.context.BaseNotifyCtx;
import com.dtp.core.notify.alarm.AlarmCounter;
import com.dtp.core.notify.alarm.AlarmLimiter;
import com.dtp.core.support.ThreadPoolBuilder;
import com.dtp.core.thread.DtpExecutor;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

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
            .threadPoolName("dtp-alarm")
            .threadFactory("dtp-alarm")
            .corePoolSize(2)
            .maximumPoolSize(4)
            .workQueue(LINKED_BLOCKING_QUEUE.getName(), 2000, false, null)
            .rejectedExecutionHandler(RejectedTypeEnum.DISCARD_OLDEST_POLICY.getName())
            .buildCommon();

    private static final InvokerChain<BaseNotifyCtx> ALARM_INVOKER_CHAIN;

    static {
        ALARM_INVOKER_CHAIN = NotifyFilterBuilder.getAlarmInvokerChain();
    }

    private AlarmManager() { }

    public static void initAlarm(DtpExecutor executor, List<NotifyPlatform> platforms) {
        if (CollUtil.isEmpty(platforms)) {
            executor.setNotifyItems(Lists.newArrayList());
            return;
        }
        if (CollUtil.isEmpty(executor.getNotifyItems())) {
            log.warn("DynamicTp notify, no notify items configured, name {}", executor.getThreadPoolName());
            return;
        }

        NotifyItemManager.fillPlatforms(platforms, executor.getNotifyItems());
        initAlarm(executor.getThreadPoolName(), executor.getNotifyItems());
    }

    public static void initAlarm(String poolName, List<NotifyItem> notifyItems) {
        notifyItems.forEach(x -> {
            AlarmLimiter.initAlarmLimiter(poolName, x);
            AlarmCounter.init(poolName, x.getType());
        });
    }

    public static void refreshAlarm(String poolName,
                                    List<NotifyPlatform> platforms,
                                    List<NotifyItem> oldItems,
                                    List<NotifyItem> newItems) {
        if (CollectionUtils.isEmpty(newItems)) {
            return;
        }
        NotifyItemManager.fillPlatforms(platforms, newItems);
        Map<String, NotifyItem> oldNotifyItemMap = StreamUtil.toMap(oldItems, NotifyItem::getType);
        newItems.forEach(x -> {
            NotifyItem oldNotifyItem = oldNotifyItemMap.get(x.getType());
            if (Objects.nonNull(oldNotifyItem) && oldNotifyItem.getInterval() == x.getInterval()) {
                return;
            }
            AlarmLimiter.initAlarmLimiter(poolName, x);
            AlarmCounter.init(poolName, x.getType());
        });
    }

    public static void triggerAlarm(String dtpName, String notifyType, Runnable runnable) {
        AlarmCounter.incAlarmCounter(dtpName, notifyType);
        ALARM_EXECUTOR.execute(runnable);
    }

    public static void triggerAlarm(Runnable runnable) {
        ALARM_EXECUTOR.execute(runnable);
    }

    public static void doAlarm(DtpExecutor executor, List<NotifyItemEnum> notifyItemEnums) {
        val executorWrapper = new ExecutorWrapper(executor.getThreadPoolName(), executor,
                executor.getNotifyItems(), executor.isNotifyEnabled());
        doAlarm(executorWrapper, notifyItemEnums);
    }

    public static void doAlarm(ExecutorWrapper executorWrapper, List<NotifyItemEnum> notifyItemEnums) {
        notifyItemEnums.forEach(x -> doAlarm(executorWrapper, x));
    }

    public static void doAlarm(DtpExecutor executor, NotifyItemEnum notifyItemEnum) {
        val executorWrapper = new ExecutorWrapper(executor.getThreadPoolName(), executor,
                executor.getNotifyItems(), executor.isNotifyEnabled());
        doAlarm(executorWrapper, notifyItemEnum);
    }

    public static void doAlarm(ExecutorWrapper executorWrapper, NotifyItemEnum notifyItemEnum) {
        NotifyItem notifyItem = NotifyItemManager.getNotifyItem(executorWrapper, notifyItemEnum);
        if (notifyItem == null) {
            return;
        }
        AlarmCtx alarmCtx = new AlarmCtx(executorWrapper, notifyItem);
        DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
        alarmCtx.setPlatforms(dtpProperties.getPlatforms());
        ALARM_INVOKER_CHAIN.proceed(alarmCtx);
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
        val executor = (ThreadPoolExecutor) executorWrapper.getExecutor();
        int maximumPoolSize = executor.getMaximumPoolSize();
        double div = NumberUtil.div(executor.getActiveCount(), maximumPoolSize, 2) * 100;
        return div >= notifyItem.getThreshold();
    }

    private static boolean checkCapacity(ExecutorWrapper executorWrapper, NotifyItem notifyItem) {

        val executor = (ThreadPoolExecutor) executorWrapper.getExecutor();
        BlockingQueue<Runnable> workQueue = executor.getQueue();
        if (CollUtil.isEmpty(workQueue)) {
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
