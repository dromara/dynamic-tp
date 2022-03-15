package com.dtp.core.thread;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.dtp.common.VariableLinkedBlockingQueue;
import com.dtp.common.constant.DynamicTpConst;
import com.dtp.common.dto.NotifyItem;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.common.em.QueueTypeEnum;
import com.dtp.common.em.RejectedTypeEnum;
import com.dtp.core.reject.RejectHandlerGetter;
import com.dtp.core.support.TaskQueue;
import com.dtp.core.support.TaskWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.*;

/**
 * Builder for creating a ThreadPoolExecutor gracefully.
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class ThreadPoolBuilder {

    /**
     * Name of Dynamic ThreadPool.
     */
    private String threadPoolName = "DefaultDynamicTp";

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
    private long keepAliveTime = 30;

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
     * RejectedExecutionHandler, see {@link RejectedTypeEnum}
     */
    private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();

    /**
     * Default inner thread factory.
     */
    private ThreadFactory threadFactory = new NamedThreadFactory("dynamic-tp");

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
     * The task wrapper.
     */
    private TaskWrapper taskWrapper;

    /**
     * If io intensive thread pool.
     * default false, true indicate cpu intensive thread pool.
     */
    private boolean ioIntensive = false;

    /**
     * Notify items, see {@link NotifyTypeEnum}
     */
    private List<NotifyItem> notifyItems = NotifyItem.getDefaultNotifyItems();

    private ThreadPoolBuilder() {}

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
     * @param queueName queue name
     * @param capacity queue capacity
     * @param fair for SynchronousQueue
     * @return the ThreadPoolBuilder instance
     */
    public ThreadPoolBuilder workQueue(String queueName, Integer capacity, Boolean fair) {
        if (StringUtils.isNotBlank(queueName)) {
            workQueue = QueueTypeEnum.buildBlockingQueue(queueName,
                    capacity != null ? capacity : 1024, fair != null && fair);
        }
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

    public ThreadPoolBuilder notifyItems(List<NotifyItem> notifyItemList) {
        if (CollUtil.isNotEmpty(notifyItemList)) {
            notifyItems = notifyItemList;
        }
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

    public ThreadPoolBuilder taskWrapper(TaskWrapper taskWrapper) {
        this.taskWrapper = taskWrapper;
        return this;
    }

    public ThreadPoolBuilder ioIntensive(boolean ioIntensive) {
        this.ioIntensive = ioIntensive;
        return this;
    }

    public ThreadPoolBuilder queueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
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
     * @see com.alibaba.ttl.TransmittableThreadLocal
     * @return the newly created ExecutorService instance
     */
    public ExecutorService buildWithTtl() {
        if (dynamic) {
            taskWrapper = TtlRunnable::get;
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
        dtpExecutor.setNotifyItems(notifyItems);
        dtpExecutor.setWaitForTasksToCompleteOnShutdown(waitForTasksToCompleteOnShutdown);
        dtpExecutor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        dtpExecutor.setTaskWrapper(taskWrapper);
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
