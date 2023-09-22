package org.dromara.dynamictp.starter.adapter.webserver.adapter.undertow;

import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.support.task.runnable.EnhancedRunnable;
import org.jboss.threads.EnhancedQueueExecutor;

import java.util.concurrent.RejectedExecutionException;

/**
 * EnhancedQueueExecutorProxy related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@SuppressWarnings("all")
public class EnhancedQueueExecutorProxy extends EnhancedQueueExecutor {

    public EnhancedQueueExecutorProxy(final Builder builder) {
        super(builder);
    }

    public EnhancedQueueExecutorProxy(final EnhancedQueueExecutor executor) {
        this(new EnhancedQueueExecutor.Builder()
                .setCorePoolSize(executor.getCorePoolSize())
                .setMaximumPoolSize(executor.getMaximumPoolSize())
                .setKeepAliveTime(executor.getKeepAliveTime())
                .setThreadFactory(executor.getThreadFactory())
                .setTerminationTask(executor.getTerminationTask())
                .setRegisterMBean(true)
                .setMBeanName(executor.getMBeanName()));
    }

    @Override
    public void execute(Runnable runnable) {
        EnhancedRunnable enhanceTask = EnhancedRunnable.of(runnable, this);
        AwareManager.execute(this, enhanceTask);
        try {
            super.execute(enhanceTask);
        } catch (Throwable e) {
            Throwable[] suppressedExceptions = e.getSuppressed();
            for (Throwable t : suppressedExceptions) {
                if (t instanceof RejectedExecutionException) {
                    AwareManager.beforeReject(enhanceTask, this);
                    return;
                }
            }
            throw e;
        }
    }
}
