package org.dromara.dynamictp.starter.adapter.webserver.jetty;

import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.support.task.runnable.EnhancedRunnable;
import org.eclipse.jetty.util.thread.ExecutionStrategy;

import java.util.concurrent.Executor;

/**
 * @author kyao
 * @date 2023年09月25日 14:43
 */
public class SelectorProducerProxy implements ExecutionStrategy.Producer {

    private final ExecutionStrategy.Producer producer;

    private final Executor executor;

    public SelectorProducerProxy(ExecutionStrategy.Producer producer, Executor executor) {
        this.producer = producer;
        this.executor = executor;
    }

    @Override
    public Runnable produce() {
        Runnable task = producer.produce();
        EnhancedRunnable enhancedTask = EnhancedRunnable.of(task, executor);
        AwareManager.execute(executor, enhancedTask);
        return enhancedTask;
    }
}
