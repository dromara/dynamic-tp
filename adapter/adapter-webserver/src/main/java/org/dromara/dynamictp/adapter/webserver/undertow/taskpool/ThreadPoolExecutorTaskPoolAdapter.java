package org.dromara.dynamictp.adapter.webserver.undertow.taskpool;

import org.dromara.dynamictp.adapter.webserver.undertow.UndertowTaskPoolEnum;
import org.dromara.dynamictp.core.support.ExecutorAdapter;
import org.dromara.dynamictp.core.support.ThreadPoolExecutorAdapter;

import java.util.concurrent.ThreadPoolExecutor;

import static org.dromara.dynamictp.adapter.webserver.undertow.UndertowTaskPoolEnum.THREAD_POOL_EXECUTOR_TASK_POOL;

/**
 * ThreadPoolExecutorTaskPoolHandler related
 *
 * @author yanhom
 * @since 1.1.3
 */
public class ThreadPoolExecutorTaskPoolAdapter implements TaskPoolAdapter {

    @Override
    public UndertowTaskPoolEnum taskPoolType() {
        return THREAD_POOL_EXECUTOR_TASK_POOL;
    }

    @Override
    public ExecutorAdapter<ThreadPoolExecutor> adapt(Object executor) {
        return new ThreadPoolExecutorAdapter((ThreadPoolExecutor) executor);
    }
}
