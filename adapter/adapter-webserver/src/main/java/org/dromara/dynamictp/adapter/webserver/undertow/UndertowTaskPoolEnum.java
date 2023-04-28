package org.dromara.dynamictp.adapter.webserver.undertow;

import lombok.Getter;

/**
 * UndertowTaskPoolEnum related
 *
 * @author yanhom
 * @since 1.1.3
 */
@Getter
public enum UndertowTaskPoolEnum {

    /**
     * EnhancedQueueExecutorTaskPool
     */
    ENHANCED_QUEUE_EXECUTOR_TASK_POOL("EnhancedQueueExecutorTaskPool", "executor"),

    /**
     * ThreadPoolExecutorTaskPool
     */
    THREAD_POOL_EXECUTOR_TASK_POOL("ThreadPoolExecutorTaskPool", "delegate"),

    /**
     * ExternalTaskPool
     */
    EXTERNAL_TASK_POOL("ExternalTaskPool", "delegate"),

    /**
     * ExecutorServiceTaskPool
     */
    EXECUTOR_SERVICE_TASK_POOL("ExecutorServiceTaskPool", "delegate"),;

    private final String className;

    private final String internalExecutor;

    UndertowTaskPoolEnum(String className, String internalExecutor) {
        this.className = className;
        this.internalExecutor = internalExecutor;
    }
}
