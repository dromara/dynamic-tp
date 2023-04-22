package com.dtp.adapter.webserver.undertow.taskpool;

import com.dtp.adapter.webserver.undertow.UndertowTaskPoolEnum;
import com.dtp.core.support.ExecutorAdapter;
import org.jboss.threads.EnhancedQueueExecutor;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static com.dtp.adapter.webserver.undertow.UndertowTaskPoolEnum.ENHANCED_QUEUE_EXECUTOR_TASK_POOL;

/**
 * EnhancedQueueExecutorTaskPoolAdapter implements ExecutorAdapter, the goal of this class
 * is to be compatible with {@link org.jboss.threads.EnhancedQueueExecutor}.
 *
 * @author yanhom
 * @since 1.1.3
 */
public class EnhancedQueueExecutorTaskPoolAdapter implements TaskPoolAdapter {

    @Override
    public UndertowTaskPoolEnum taskPoolType() {
        return ENHANCED_QUEUE_EXECUTOR_TASK_POOL;
    }

    @Override
    public ExecutorAdapter<EnhancedQueueExecutor> adapt(Object executor) {
        return new EnhancedQueueExecutorAdapter((EnhancedQueueExecutor) executor);
    }

    private static class EnhancedQueueExecutorAdapter implements ExecutorAdapter<EnhancedQueueExecutor> {

        private final EnhancedQueueExecutor executor;

        EnhancedQueueExecutorAdapter(EnhancedQueueExecutor executor) {
            this.executor = executor;
        }

        @Override
        public EnhancedQueueExecutor getOriginal() {
            return this.executor;
        }

        @Override
        public int getCorePoolSize() {
            return this.executor.getCorePoolSize();
        }

        @Override
        public void setCorePoolSize(int corePoolSize) {
            this.executor.setCorePoolSize(corePoolSize);
        }

        @Override
        public int getMaximumPoolSize() {
            return this.executor.getMaximumPoolSize();
        }

        @Override
        public void setMaximumPoolSize(int maximumPoolSize) {
            this.executor.setMaximumPoolSize(maximumPoolSize);
        }

        @Override
        public int getPoolSize() {
            return this.executor.getPoolSize();
        }

        @Override
        public int getActiveCount() {
            return this.executor.getActiveCount();
        }

        @Override
        public int getLargestPoolSize() {
            return this.executor.getLargestPoolSize();
        }

        @Override
        public long getCompletedTaskCount() {
            return this.executor.getCompletedTaskCount();
        }

        @Override
        public int getQueueCapacity() {
            return this.executor.getMaximumQueueSize();
        }

        @Override
        public int getQueueSize() {
            return this.executor.getQueueSize();
        }

        @Override
        public int getQueueRemainingCapacity() {
            return this.getQueueCapacity() - this.getQueueSize();
        }

        @Override
        public boolean allowsCoreThreadTimeOut() {
            return this.executor.allowsCoreThreadTimeOut();
        }

        @Override
        public void allowCoreThreadTimeOut(boolean value) {
            this.executor.allowCoreThreadTimeOut(value);
        }

        @Override
        public long getKeepAliveTime(TimeUnit unit) {
            return this.executor.getKeepAliveTime().getSeconds();
        }

        @Override
        public void setKeepAliveTime(long time, TimeUnit unit) {
            this.executor.setKeepAliveTime(Duration.of(time, ChronoUnit.SECONDS));
        }

        @Override
        public long getRejectedTaskCount() {
            return this.executor.getRejectedTaskCount();
        }
    }
}
