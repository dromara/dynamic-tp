package com.dtp.core.thread;

import cn.hutool.core.collection.CollUtil;
import com.dtp.common.dto.NotifyItem;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.core.notify.AlarmManager;
import com.dtp.core.reject.RejectHandlerGetter;
import com.dtp.core.spring.DtpLifecycleSupport;
import com.dtp.core.support.DtpRunnable;
import com.dtp.core.support.wrapper.TaskWrapper;
import com.google.common.collect.Lists;

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
 * @author: yanhom
 * @since 1.0.0
 **/
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

        if (preStartAllCoreThreads) {
            prestartAllCoreThreads();
        }
    }

    @Override
    public void execute(Runnable command) {
        if (CollUtil.isNotEmpty(taskWrappers)) {
            for (TaskWrapper t : taskWrappers) {
                command = t.wrap(command);
            }
        }

        if (runTimeout > 0 || queueTimeout > 0) {
            command = new DtpRunnable(command);
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
            }
        }

        super.afterExecute(r, t);
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
}
