package com.dtp.common.config;

import com.dtp.common.constant.DynamicTpConst;
import com.dtp.common.dto.NotifyItem;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.common.em.QueueTypeEnum;
import com.dtp.common.em.RejectedTypeEnum;
import lombok.Data;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ThreadPool main properties.
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Data
public class ThreadPoolProperties {

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
     * BlockingQueue capacity.
     */
    private int queueCapacity = 1024;

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
     * Notify items, see {@link NotifyTypeEnum}
     */
    private List<NotifyItem> notifyItems;

}
