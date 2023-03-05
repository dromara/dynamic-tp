package com.dtp.common.properties;

import com.dtp.common.constant.DynamicTpConst;
import com.dtp.common.entity.NotifyItem;
import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.em.QueueTypeEnum;
import com.dtp.common.em.RejectedTypeEnum;
import com.dtp.common.entity.NotifyPlatform;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * ThreadPool main properties.
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Data
public class ThreadPoolProperties {

    /**
     * Name of Dynamic ThreadPool.
     */
    private String threadPoolName = "DynamicTp";

    /**
     * Simple Alias Name of Dynamic ThreadPool. Use for notify.
     */
    private String threadPoolAliasName;

    /**
     * Executor type, used in create phase.
     */
    private String executorType;

    /**
     * CoreSize of ThreadPool.
     */
    private int corePoolSize = 1;

    /**
     * MaxSize of ThreadPool.
     */
    private int maximumPoolSize = DynamicTpConst.AVAILABLE_PROCESSORS;

    /**
     * BlockingQueue capacity.
     */
    private int queueCapacity = 1024;

    /**
     * Max free memory for MemorySafeLBQ, unit M
     */
    private int maxFreeMemory = 256;

    /**
     * Blocking queue type, see {@link QueueTypeEnum}
     */
    private String queueType = QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE.getName();

    /**
     * If fair strategy, for SynchronousQueue
     */
    private boolean fair = false;

    /**
     * RejectedExecutionHandler type, see {@link RejectedTypeEnum}
     */
    private String rejectedHandlerType = RejectedTypeEnum.ABORT_POLICY.getName();

    /**
     * When the number of threads is greater than the core,
     * this is the maximum time that excess idle threads
     * will wait for new tasks before terminating.
     */
    private long keepAliveTime = 30;

    /**
     * Timeout unit.
     */
    private TimeUnit unit = TimeUnit.SECONDS;

    /**
     * If allow core thread timeout.
     */
    private boolean allowCoreThreadTimeOut = false;

    /**
     * Thread name prefix.
     */
    private String threadNamePrefix = "dynamic-tp";

    /**
     * Notify items, see {@link NotifyItemEnum}
     */
    private List<NotifyItem> notifyItems;

    /**
     * If enable notify.
     */
    private boolean notifyEnabled = true;

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
     * Task wrapper names.
     */
    private Set<String> taskWrapperNames;

    /**
     * Notify platform configs.
     */
    private List<NotifyPlatform> platforms;

    /**
     * 检查核心参数
     *
     * @return boolean return true means params is inValid
     */
    public boolean coreParamIsInValid() {
        return this.getCorePoolSize() < 0
                || this.getKeepAliveTime() < 0
                || this.getMaximumPoolSize() <= 0
                || this.getMaximumPoolSize() < this.getCorePoolSize();
    }
}
