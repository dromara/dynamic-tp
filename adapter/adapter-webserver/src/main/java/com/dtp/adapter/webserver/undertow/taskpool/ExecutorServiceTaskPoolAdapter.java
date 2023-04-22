package com.dtp.adapter.webserver.undertow.taskpool;

import com.dtp.adapter.webserver.undertow.UndertowTaskPoolEnum;
import com.dtp.core.support.ExecutorAdapter;
import com.dtp.core.support.ThreadPoolExecutorAdapter;

import java.util.concurrent.ThreadPoolExecutor;

import static com.dtp.adapter.webserver.undertow.UndertowTaskPoolEnum.EXECUTOR_SERVICE_TASK_POOL;

/**
 * ExecutorServiceTaskPoolAdapter related
 *
 * @author yanhom
 * @since 1.1.3
 */
public class ExecutorServiceTaskPoolAdapter implements TaskPoolAdapter {

    @Override
    public UndertowTaskPoolEnum taskPoolType() {
        return EXECUTOR_SERVICE_TASK_POOL;
    }

    @Override
    public ExecutorAdapter<ThreadPoolExecutor> adapt(Object executor) {
        return new ThreadPoolExecutorAdapter((ThreadPoolExecutor) executor);
    }
}
