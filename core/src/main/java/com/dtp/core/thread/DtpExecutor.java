package com.dtp.core.thread;

import cn.hutool.core.collection.CollUtil;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.dto.NotifyItem;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.core.notify.alarm.AlarmManager;
import com.dtp.core.reject.RejectHandlerGetter;
import com.dtp.core.spring.DtpLifecycleSupport;
import com.dtp.core.support.runnable.DtpRunnable;
import com.dtp.core.support.runnable.NamedRunnable;
import com.dtp.core.support.wrapper.TaskWrapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dtp.common.em.NotifyTypeEnum.QUEUE_TIMEOUT;
import static com.dtp.common.em.NotifyTypeEnum.RUN_TIMEOUT;

/**
 * Dynamic ThreadPoolExecutor inherits DtpLifecycleSupport, and extends some features.
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpExecutor extends DtpLifecycleSupport {

    /**
     * Total reject count.
     */
    private final AtomicInteger rejectCount = new AtomicInteger(0);

    /**
     * RejectHandler name.
     */
    private String rejectHandlerName;

    /**
     * Simple Business alias Name of Dynamic ThreadPool. Use for notify.
     */
    private String threadPoolAliasName;

    /**
     * Notify items, see {@link NotifyTypeEnum}.
     */
    private List<NotifyItem> notifyItems;

    /**
     * Task wrappers, do sth enhanced.
     */
    private List<TaskWrapper> taskWrappers = Lists.newArrayList();

    /**
     * If pre start all core threads.
     */
    private boolean preStartAllCoreThreads;

    /**
     * Task execute timeout, unit (ms), just for statistics.
     */
    private long runTimeout;

    /**
     * Task queue wait timeout, unit (ms), just for statistics.
     */
    private long queueTimeout;

    /**
     * Count run timeout tasks.
     */
    private final AtomicInteger runTimeoutCount = new AtomicInteger();

    /**
     * Count queue wait timeout tasks.
     */
    private final AtomicInteger queueTimeoutCount = new AtomicInteger();

    public DtpExecutor(int corePoolSize,
                       int maximumPoolSize,
                       long keepAliveTime,
                       TimeUnit unit,
                       BlockingQueue<Runnable> workQueue,
                       ThreadFactory threadFactory,
                       RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.rejectHandlerName = handler.getClass().getSimpleName();
        RejectedExecutionHandler rejectedExecutionHandler = RejectHandlerGetter.getProxy(handler);
        setRejectedExecutionHandler(rejectedExecutionHandler);
    }

    @Override
    public void execute(Runnable command) {
        String taskName = null;
        if (command instanceof NamedRunnable) {
            taskName = ((NamedRunnable) command).getName();
        }

        if (CollUtil.isNotEmpty(taskWrappers)) {
            for (TaskWrapper t : taskWrappers) {
                command = t.wrap(command);
            }
        }

        if (runTimeout > 0 || queueTimeout > 0) {
            command = new DtpRunnable(command, taskName);
        }
        super.execute(command);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        if (!(r instanceof DtpRunnable)) {
            super.beforeExecute(t, r);
            return;
        }
        DtpRunnable runnable = (DtpRunnable) r;
        long currTime = System.currentTimeMillis();
        if (runTimeout > 0) {
            runnable.setStartTime(currTime);
        }
        if (queueTimeout > 0) {
            long waitTime = currTime - runnable.getSubmitTime();
            if (waitTime > queueTimeout) {
                queueTimeoutCount.incrementAndGet();
                Runnable alarmTask = () -> AlarmManager.doAlarm(this, QUEUE_TIMEOUT);
                AlarmManager.triggerAlarm(this.getThreadPoolName(), QUEUE_TIMEOUT.getValue(), alarmTask);
                if (StringUtils.isNotBlank(runnable.getTaskName())) {
                    log.warn("DynamicTp execute, queue timeout, poolName: {}, taskName: {}, waitTime: {}ms",
                            this.getThreadPoolName(), runnable.getTaskName(), waitTime);
                }
            }
        }

        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {

        if (runTimeout > 0) {
            DtpRunnable runnable = (DtpRunnable) r;
            long runTime = System.currentTimeMillis() - runnable.getStartTime();
            if (runTime > runTimeout) {
                runTimeoutCount.incrementAndGet();
                Runnable alarmTask = () -> AlarmManager.doAlarm(this, RUN_TIMEOUT);
                AlarmManager.triggerAlarm(this.getThreadPoolName(), RUN_TIMEOUT.getValue(), alarmTask);
                if (StringUtils.isNotBlank(runnable.getTaskName())) {
                    log.warn("DynamicTp execute, run timeout, poolName: {}, taskName: {}, runTime: {}ms",
                            this.getThreadPoolName(), runnable.getTaskName(), runTime);
                }
            }
        }

        super.afterExecute(r, t);
    }

    @Override
    protected void initialize(DtpProperties dtpProperties) {
        AlarmManager.initAlarm(this, dtpProperties.getPlatforms());

        if (preStartAllCoreThreads) {
            prestartAllCoreThreads();
        }
    }

    public void incRejectCount(int count) {
        rejectCount.addAndGet(count);
    }

    public int getRejectCount() {
        return rejectCount.get();
    }

    public List<NotifyItem> getNotifyItems() {
        return notifyItems;
    }

    public void setNotifyItems(List<NotifyItem> notifyItems) {
        this.notifyItems = notifyItems;
    }

    public String getQueueName() {
        return getQueue().getClass().getSimpleName();
    }

    public int getQueueCapacity() {
        int capacity = getQueue().size() + getQueue().remainingCapacity();
        return capacity < 0 ? Integer.MAX_VALUE : capacity;
    }

    public String getRejectHandlerName() {
        return rejectHandlerName;
    }

    public void setRejectHandlerName(String rejectHandlerName) {
        this.rejectHandlerName = rejectHandlerName;
    }

    public void setTaskWrappers(List<TaskWrapper> taskWrappers) {
        this.taskWrappers = taskWrappers;
    }

    public void setPreStartAllCoreThreads(boolean preStartAllCoreThreads) {
        this.preStartAllCoreThreads = preStartAllCoreThreads;
    }

    public void setRunTimeout(long runTimeout) {
        this.runTimeout = runTimeout;
    }

    public int getRunTimeoutCount() {
        return runTimeoutCount.get();
    }

    public int getQueueTimeoutCount() {
        return queueTimeoutCount.get();
    }

    public void setQueueTimeout(long queueTimeout) {
        this.queueTimeout = queueTimeout;
    }

    /**
     * In order for the field can be assigned by reflection.
     * @param allowCoreThreadTimeOut allowCoreThreadTimeOut
     */
    public void setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        allowCoreThreadTimeOut(allowCoreThreadTimeOut);
    }

    public String getThreadPoolAliasName() {
        return threadPoolAliasName;
    }

    public void setThreadPoolAliasName(String threadPoolAliasName) {
        this.threadPoolAliasName = threadPoolAliasName;
    }
}
