package org.dromara.dynamictp.adapter.webserver.undertow.taskpool;

import org.dromara.dynamictp.adapter.webserver.undertow.UndertowTaskPoolEnum;
import org.dromara.dynamictp.core.support.ExecutorAdapter;

import java.util.concurrent.Executor;

/**
 * TaskPoolAdapter related
 *
 * @author yanhom
 * @since 1.1.3
 */
public interface TaskPoolAdapter {

    /**
     * Get the task pool type
     *
     * @return task pool type
     */
    UndertowTaskPoolEnum taskPoolType();

    /**
     * Adapt the task pool
     *
     * @param taskPool task pool
     * @return executor adapter
     */
    ExecutorAdapter<? extends Executor> adapt(Object taskPool);
}
