package com.dtp.core.support;

import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.dtp.common.constant.DynamicTpConst;
import com.dtp.common.entity.NotifyItem;
import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.em.QueueTypeEnum;
import com.dtp.common.em.RejectedTypeEnum;
import com.dtp.common.queue.VariableLinkedBlockingQueue;
import com.dtp.core.reject.RejectHandlerGetter;
import com.dtp.core.support.wrapper.TaskWrapper;
import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.thread.ScheduledDtpExecutor;
import com.dtp.core.thread.EagerDtpExecutor;
import com.dtp.core.thread.NamedThreadFactory;
import com.dtp.core.thread.OrderedDtpExecutor;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Builder for creating a ThreadPoolExecutor gracefully.
 *
 * @author yanhom
 * @since 1.0.0
 **/
public class ThreadPoolBuilder {

    /**
     * Name of Dynamic ThreadPool.
     */
    private String threadPoolName = "DynamicTp";

    /**
     * CoreSize of ThreadPool.
     */
    private int corePoolSize = 1;

    /**
     * MaxSize of ThreadPool.
     */
    private int maximumPoolSize = DynamicTpConst.AVAILABLE_PROCESSORS;

    /**
     * When the number of threads is greater than the core,
     * this is the maximum time that excess idle threads
     * will wait for new tasks before terminating
     */
    private long keepAliveTime = 60;

    /**
     * Timeout unit.
     */
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    /**
     * Blocking queue, see {@link QueueTypeEnum}
     */
    private BlockingQueue<Runnable> workQueue = new VariableLinkedBlockingQueue<>(1024);

    /**
     * Queue capacity
     */
    private int queueCapacity = 1024;

    /**
     * Max free memory for MemorySafeLBQ, unit M
     */
    private int maxFreeMemory = 256;

    /**
     * RejectedExecutionHandler, see {@link RejectedTypeEnum}
     */
    private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();

    /**
     * Default inner thread factory.
     */
    private ThreadFactory threadFactory = new NamedThreadFactory("dtp");

    /**
     * If allow core thread timeout.
     */
    private boolean allowCoreThreadTimeOut = false;

    /**
     * Dynamic or common.
     */
    private boolean dynamic = true;

    /**
     * Whether to wait for scheduled tasks to complete on shutdown,
     * not interrupting running tasks and executing all tasks in the queue.
     */
    private boolean waitForTasksToCompleteOnShutdown = false;

    /**
     * The maximum number of seconds that this executor is supposed to block
     * on shutdown in order to wait for remaining tasks to complete their execution
     * before the rest of the container continues to shut down.
     */
    private int awaitTerminationSeconds = 0;

    /**
     * If io intensive thread pool.
     * default false, true indicate cpu intensive thread pool.
     */
    private boolean ioIntensive = false;

    /**
     * If ordered thread pool.
     * default false, true ordered thread pool.
     */
    private boolean ordered = false;

    /**
     * If scheduled executor, default false.
     */
    private boolean scheduled = false;

    /**
     * If pre start all core threads.
     */
    private boolean preStartAllCoreThreads = false;

    /**
     * Task execute timeout, unit (ms), just for statistics.
     */
    private long runTimeout = 0;

    /**
     * Task queue wait timeout, unit (ms), just for statistics.
     */
    private long queueTimeout = 0;

    /**
     * Task wrappers.
     */
    private final List<TaskWrapper> taskWrappers = Lists.newArrayList();

    /**
     * Notify items, see {@link NotifyItemEnum}
     */
    private List<NotifyItem> notifyItems = NotifyItem.getAllNotifyItems();

    /**
     * Notify platform id
     */
    private List<String> platformIds = Lists.newArrayList();

    private ThreadPoolBuilder() {
    }

    public static ThreadPoolBuilder newBuilder() {
        return new ThreadPoolBuilder();
    }

    public ThreadPoolBuilder threadPoolName(String poolName) {
        this.threadPoolName = poolName;
        return this;
    }

    public ThreadPoolBuilder corePoolSize(int corePoolSize) {
        if (corePoolSize >= 0) {
            this.corePoolSize = corePoolSize;
        }
        return this;
    }

    public ThreadPoolBuilder maximumPoolSize(int maximumPoolSize) {
        if (maximumPoolSize > 0) {
            this.maximumPoolSize = maximumPoolSize;
        }
        return this;
    }

    public ThreadPoolBuilder keepAliveTime(long keepAliveTime) {
        if (keepAliveTime > 0) {
            this.keepAliveTime = keepAliveTime;
        }
        return this;
    }

    public ThreadPoolBuilder timeUnit(TimeUnit timeUnit) {
        if (timeUnit != null) {
            this.timeUnit = timeUnit;
        }
        return this;
    }

    /**
     * Create work queue
     *
     * @param queueName     queue name
     * @param capacity      queue capacity
     * @param fair          for SynchronousQueue
     * @param maxFreeMemory for MemorySafeLBQ
     * @return the ThreadPoolBuilder instance
     */
    public ThreadPoolBuilder workQueue(String queueName, Integer capacity, Boolean fair, Integer maxFreeMemory) {
        if (StringUtils.isNotBlank(queueName)) {
            workQueue = QueueTypeEnum.buildLbq(queueName, capacity != null ? capacity : this.queueCapacity,
                    fair != null && fair, maxFreeMemory != null ? maxFreeMemory : this.maxFreeMemory);
        }
        return this;
    }

    /**
     * Create work queue
     *
     * @param queueName queue name
     * @param capacity  queue capacity
     * @param fair      for SynchronousQueue
     * @return the ThreadPoolBuilder instance
     */
    public ThreadPoolBuilder workQueue(String queueName, Integer capacity, Boolean fair) {
        if (StringUtils.isNotBlank(queueName)) {
            workQueue = QueueTypeEnum.buildLbq(queueName, capacity != null ? capacity : this.queueCapacity,
                    fair != null && fair, maxFreeMemory);
        }
        return this;
    }

    public ThreadPoolBuilder queueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
        return this;
    }

    public ThreadPoolBuilder maxFreeMemory(int maxFreeMemory) {
        this.maxFreeMemory = maxFreeMemory;
        return this;
    }

    public ThreadPoolBuilder rejectedExecutionHandler(String rejectedName) {
        if (StringUtils.isNotBlank(rejectedName)) {
            rejectedExecutionHandler = RejectHandlerGetter.buildRejectedHandler(rejectedName);
        }
        return this;
    }

    public ThreadPoolBuilder threadFactory(String prefix) {
        if (StringUtils.isNotBlank(prefix)) {
            threadFactory = new NamedThreadFactory(prefix);
        }
        return this;
    }

    public ThreadPoolBuilder allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        return this;
    }

    public ThreadPoolBuilder dynamic(boolean dynamic) {
        this.dynamic = dynamic;
        return this;
    }

    public ThreadPoolBuilder awaitTerminationSeconds(int awaitTerminationSeconds) {
        this.awaitTerminationSeconds = awaitTerminationSeconds;
        return this;
    }

    public ThreadPoolBuilder waitForTasksToCompleteOnShutdown(boolean waitForTasksToCompleteOnShutdown) {
        this.waitForTasksToCompleteOnShutdown = waitForTasksToCompleteOnShutdown;
        return this;
    }

    public ThreadPoolBuilder ioIntensive(boolean ioIntensive) {
        this.ioIntensive = ioIntensive;
        return this;
    }

    public ThreadPoolBuilder ordered(boolean ordered) {
        this.ordered = ordered;
        return this;
    }

    public ThreadPoolBuilder scheduled(boolean scheduled) {
        this.scheduled = scheduled;
        return this;
    }

    public ThreadPoolBuilder preStartAllCoreThreads(boolean preStartAllCoreThreads) {
        this.preStartAllCoreThreads = preStartAllCoreThreads;
        return this;
    }

    public ThreadPoolBuilder runTimeout(long runTimeout) {
        this.runTimeout = runTimeout;
        return this;
    }

    public ThreadPoolBuilder queueTimeout(long queueTimeout) {
        this.queueTimeout = queueTimeout;
        return this;
    }

    public ThreadPoolBuilder taskWrappers(List<TaskWrapper> taskWrappers) {
        this.taskWrappers.addAll(taskWrappers);
        return this;
    }

    public ThreadPoolBuilder taskWrapper(TaskWrapper taskWrapper) {
        this.taskWrappers.add(taskWrapper);
        return this;
    }

    public ThreadPoolBuilder notifyItems(List<NotifyItem> notifyItemList) {
        if (CollectionUtils.isNotEmpty(notifyItemList)) {
            notifyItems = notifyItemList;
        }
        return this;
    }

    public ThreadPoolBuilder platformIds(List<String> platformIds) {
        if (CollectionUtils.isNotEmpty(platformIds)) {
            this.platformIds = platformIds;
        }
        return this;
    }

    /**
     * Build according to dynamic field.
     *
     * @return the newly created ThreadPoolExecutor instance
     */
    public ThreadPoolExecutor build() {
        if (dynamic) {
            return buildDtpExecutor(this);
        } else {
            return buildCommonExecutor(this);
        }
    }

    /**
     * Build a dynamic ThreadPoolExecutor.
     *
     * @return the newly created DtpExecutor instance
     */
    public DtpExecutor buildDynamic() {
        return buildDtpExecutor(this);
    }

    /**
     * Build common ThreadPoolExecutor.
     *
     * @return the newly created ThreadPoolExecutor instance
     */
    public ThreadPoolExecutor buildCommon() {
        return buildCommonExecutor(this);
    }

    /**
     * Build thread pool executor and wrapper with ttl
     *
     * @return the newly created ExecutorService instance
     * @see com.alibaba.ttl.TransmittableThreadLocal
     */
    public ExecutorService buildWithTtl() {
        if (dynamic) {
            taskWrappers.add(TtlRunnable::get);
            return buildDtpExecutor(this);
        } else {
            return TtlExecutors.getTtlExecutorService(buildCommonExecutor(this));
        }
    }

    /**
     * Build dynamic threadPoolExecutor.
     *
     * @param builder the targeted builder
     * @return the newly created DtpExecutor instance
     */
    private DtpExecutor buildDtpExecutor(ThreadPoolBuilder builder) {
        Assert.notNull(builder.threadPoolName, "The thread pool name must not be null.");
        DtpExecutor dtpExecutor = createInternal(builder);
        dtpExecutor.setThreadPoolName(builder.threadPoolName);
        dtpExecutor.allowCoreThreadTimeOut(builder.allowCoreThreadTimeOut);
        dtpExecutor.setWaitForTasksToCompleteOnShutdown(builder.waitForTasksToCompleteOnShutdown);
        dtpExecutor.setAwaitTerminationSeconds(builder.awaitTerminationSeconds);
        dtpExecutor.setPreStartAllCoreThreads(builder.preStartAllCoreThreads);
        dtpExecutor.setRunTimeout(builder.runTimeout);
        dtpExecutor.setQueueTimeout(builder.queueTimeout);
        dtpExecutor.setTaskWrappers(builder.taskWrappers);
        dtpExecutor.setNotifyItems(builder.notifyItems);
        dtpExecutor.setPlatformIds(builder.platformIds);
        return dtpExecutor;
    }

    private DtpExecutor createInternal(ThreadPoolBuilder builder) {
        DtpExecutor dtpExecutor;
        if (ioIntensive) {
            TaskQueue taskQueue = new TaskQueue(builder.queueCapacity);
            dtpExecutor = new EagerDtpExecutor(
                    builder.corePoolSize,
                    builder.maximumPoolSize,
                    builder.keepAliveTime,
                    builder.timeUnit,
                    taskQueue,
                    builder.threadFactory,
                    builder.rejectedExecutionHandler);
            taskQueue.setExecutor((EagerDtpExecutor) dtpExecutor);
        } else if (ordered) {
            dtpExecutor = new OrderedDtpExecutor(
                    builder.corePoolSize,
                    builder.maximumPoolSize,
                    builder.keepAliveTime,
                    builder.timeUnit,
                    builder.workQueue,
                    builder.threadFactory,
                    builder.rejectedExecutionHandler);
        } else if (scheduled) {
            dtpExecutor = new ScheduledDtpExecutor(
                    builder.corePoolSize,
                    builder.maximumPoolSize,
                    builder.keepAliveTime,
                    builder.timeUnit,
                    builder.workQueue,
                    builder.threadFactory,
                    builder.rejectedExecutionHandler);
        } else {
            dtpExecutor = new DtpExecutor(
                    builder.corePoolSize,
                    builder.maximumPoolSize,
                    builder.keepAliveTime,
                    builder.timeUnit,
                    builder.workQueue,
                    builder.threadFactory,
                    builder.rejectedExecutionHandler);
        }
        return dtpExecutor;
    }

    /**
     * Build common threadPoolExecutor, does not manage by DynamicTp framework.
     *
     * @param builder the targeted builder
     * @return the newly created ThreadPoolExecutor instance
     */
    private ThreadPoolExecutor buildCommonExecutor(ThreadPoolBuilder builder) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                builder.corePoolSize,
                builder.maximumPoolSize,
                builder.keepAliveTime,
                builder.timeUnit,
                builder.workQueue,
                builder.threadFactory,
                builder.rejectedExecutionHandler
        );
        executor.allowCoreThreadTimeOut(builder.allowCoreThreadTimeOut);
        return executor;
    }
}
