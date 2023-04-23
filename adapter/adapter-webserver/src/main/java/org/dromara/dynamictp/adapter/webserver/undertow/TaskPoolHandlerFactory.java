package org.dromara.dynamictp.adapter.webserver.undertow;

import org.dromara.dynamictp.adapter.webserver.undertow.taskpool.EnhancedQueueExecutorTaskPoolAdapter;
import org.dromara.dynamictp.adapter.webserver.undertow.taskpool.ExecutorServiceTaskPoolAdapter;
import org.dromara.dynamictp.adapter.webserver.undertow.taskpool.ExternalTaskPoolAdapter;
import org.dromara.dynamictp.adapter.webserver.undertow.taskpool.TaskPoolAdapter;
import org.dromara.dynamictp.adapter.webserver.undertow.taskpool.ThreadPoolExecutorTaskPoolAdapter;

import java.util.HashMap;
import java.util.Map;

import static org.dromara.dynamictp.adapter.webserver.undertow.UndertowTaskPoolEnum.ENHANCED_QUEUE_EXECUTOR_TASK_POOL;
import static org.dromara.dynamictp.adapter.webserver.undertow.UndertowTaskPoolEnum.EXECUTOR_SERVICE_TASK_POOL;
import static org.dromara.dynamictp.adapter.webserver.undertow.UndertowTaskPoolEnum.EXTERNAL_TASK_POOL;
import static org.dromara.dynamictp.adapter.webserver.undertow.UndertowTaskPoolEnum.THREAD_POOL_EXECUTOR_TASK_POOL;

/**
 * TaskPoolHandlerFactory related
 *
 * @author yanhom
 * @since 1.1.3
 */
public class TaskPoolHandlerFactory {

    private TaskPoolHandlerFactory() { }

    private static final Map<String, TaskPoolAdapter> TASK_POOL_HANDLERS = new HashMap<>();

    static {
        TASK_POOL_HANDLERS.put(EXTERNAL_TASK_POOL.getClassName(), new ExternalTaskPoolAdapter());
        TASK_POOL_HANDLERS.put(ENHANCED_QUEUE_EXECUTOR_TASK_POOL.getClassName(), new EnhancedQueueExecutorTaskPoolAdapter());
        TASK_POOL_HANDLERS.put(THREAD_POOL_EXECUTOR_TASK_POOL.getClassName(), new ThreadPoolExecutorTaskPoolAdapter());
        TASK_POOL_HANDLERS.put(EXECUTOR_SERVICE_TASK_POOL.getClassName(), new ExecutorServiceTaskPoolAdapter());
    }

    public static TaskPoolAdapter getTaskPoolHandler(String className) {
        return TASK_POOL_HANDLERS.get(className);
    }
}
