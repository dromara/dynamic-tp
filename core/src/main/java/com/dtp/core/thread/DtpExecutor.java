package com.dtp.core.thread;

import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.entity.NotifyItem;
import com.dtp.common.util.TimeUtil;
import com.dtp.core.notify.manager.AlarmManager;
import com.dtp.core.notify.manager.NotifyHelper;
import com.dtp.core.reject.RejectHandlerGetter;
import com.dtp.core.spring.DtpLifecycleSupport;
import com.dtp.core.spring.SpringExecutor;
import com.dtp.core.support.runnable.DtpRunnable;
import com.dtp.core.support.runnable.NamedRunnable;
import com.dtp.core.support.wrapper.TaskWrapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

import static com.dtp.common.constant.DynamicTpConst.TRACE_ID;
import static com.dtp.common.em.NotifyItemEnum.QUEUE_TIMEOUT;
import static com.dtp.common.em.NotifyItemEnum.RUN_TIMEOUT;

/**
 * Dynamic ThreadPoolExecutor inherits DtpLifecycleSupport, and extends some features.
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpExecutor extends DtpLifecycleSupport implements SpringExecutor {

    /**
     * Simple Business alias Name of Dynamic ThreadPool. Use for notify.
     */
    private String threadPoolAliasName;

    /**
     * RejectHandler name.
     */
    private String rejectHandlerName;

    /**
     * If enable notify.
     */
    private boolean notifyEnabled;

    /**
     * Notify items, see {@link NotifyItemEnum}.
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
     * Total reject count.
     */
    private final LongAdder rejectCount = new LongAdder();

    /**
     * Count run timeout tasks.
     */
    private final LongAdder runTimeoutCount = new LongAdder();

    /**
     * Count queue wait timeout tasks.
     */
    private final LongAdder queueTimeoutCount = new LongAdder();

    public DtpExecutor(int corePoolSize,
                       int maximumPoolSize,
                       long keepAliveTime,
                       TimeUnit unit,
                       BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                Executors.defaultThreadFactory(), new AbortPolicy());
    }

    public DtpExecutor(int corePoolSize,
                       int maximumPoolSize,
                       long keepAliveTime,
                       TimeUnit unit,
                       BlockingQueue<Runnable> workQueue,
                       ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                threadFactory, new AbortPolicy());
    }

    public DtpExecutor(int corePoolSize,
                       int maximumPoolSize,
                       long keepAliveTime,
                       TimeUnit unit,
                       BlockingQueue<Runnable> workQueue,
                       RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                Executors.defaultThreadFactory(), handler);
    }

    public DtpExecutor(int corePoolSize,
                       int maximumPoolSize,
                       long keepAliveTime,
                       TimeUnit unit,
                       BlockingQueue<Runnable> workQueue,
                       ThreadFactory threadFactory,
                       RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                threadFactory, RejectHandlerGetter.getProxy(handler));
        this.rejectHandlerName = handler.getClass().getSimpleName();
    }

    @Override
    public void execute(Runnable task, long startTimeout) {
        execute(task);
    }

    @Override
    public void execute(Runnable command) {
        super.execute(wrapTasks(command));
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        if (!(r instanceof DtpRunnable)) {
            return;
        }
        DtpRunnable runnable = (DtpRunnable) r;
        long currTime = TimeUtil.currentTimeMillis();
        if (runTimeout > 0) {
            runnable.setStartTime(currTime);
        }
        if (queueTimeout <= 0) {
            return;
        }
        long waitTime = currTime - runnable.getSubmitTime();
        if (waitTime > queueTimeout) {
            queueTimeoutCount.increment();
            AlarmManager.doAlarmAsync(this, QUEUE_TIMEOUT, r);
            if (StringUtils.isNotBlank(runnable.getTaskName())) {
                log.warn("DynamicTp execute, queue timeout, poolName: {}, taskName: {}, waitTime: {}ms",
                        this.getThreadPoolName(), runnable.getTaskName(), waitTime);
            }
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        tryPrintError(r, t);

        if (runTimeout <= 0) {
            clearContext();
            return;
        }
        DtpRunnable runnable = (DtpRunnable) r;
        long runTime = TimeUtil.currentTimeMillis() - runnable.getStartTime();
        if (runTime > runTimeout) {
            runTimeoutCount.increment();
            AlarmManager.doAlarmAsync(this, RUN_TIMEOUT);
            if (StringUtils.isNotBlank(runnable.getTaskName())) {
                log.warn("DynamicTp execute, run timeout, poolName: {}, taskName: {}, runTime: {}ms",
                        this.getThreadPoolName(), runnable.getTaskName(), runTime);
            }
        }
        clearContext();
    }

    @Override
    protected void initialize() {
        NotifyHelper.initNotify(this);

        if (preStartAllCoreThreads) {
            prestartAllCoreThreads();
        }
    }

    protected Runnable wrapTasks(Runnable command) {
        if (CollectionUtils.isNotEmpty(taskWrappers)) {
            for (TaskWrapper t : taskWrappers) {
                command = t.wrap(command);
            }
        }
        if (runTimeout > 0 || queueTimeout > 0) {
            String taskName = (command instanceof NamedRunnable) ? ((NamedRunnable) command).getName() : null;
            command = new DtpRunnable(command, taskName);
        }
        return command;
    }

    private void clearContext() {
        MDC.remove(TRACE_ID);
    }

    private void tryPrintError(Runnable r, Throwable t) {
        if (Objects.nonNull(t)) {
            log.error("thread {} throw exception {}", Thread.currentThread(), t);
            return;
        }
        if (r instanceof FutureTask) {
            try {
                Future<?> future = (Future<?>) r;
                future.get();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("thread {} throw exception {}", Thread.currentThread(), e);
            }
        }
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

    public void incRejectCount(int count) {
        rejectCount.add(count);
    }

    public long getRejectCount() {
        return rejectCount.sum();
    }

    public void setRunTimeout(long runTimeout) {
        this.runTimeout = runTimeout;
    }

    public long getRunTimeoutCount() {
        return runTimeoutCount.sum();
    }

    public long getQueueTimeoutCount() {
        return queueTimeoutCount.sum();
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

    public boolean isNotifyEnabled() {
        return notifyEnabled;
    }

    public void setNotifyEnabled(boolean notifyEnabled) {
        this.notifyEnabled = notifyEnabled;
    }
}
