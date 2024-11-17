/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.starter.adapter.webserver.undertow.taskpool;

import org.dromara.dynamictp.core.support.adapter.ExecutorAdapter;
import org.dromara.dynamictp.starter.adapter.webserver.undertow.UndertowTaskPoolEnum;
import org.jboss.threads.EnhancedQueueExecutor;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

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
        return UndertowTaskPoolEnum.ENHANCED_QUEUE_EXECUTOR_TASK_POOL;
    }

    @Override
    public ExecutorAdapter<EnhancedQueueExecutor> adapt(Object executor) {
        return new EnhancedQueueExecutorAdapter((EnhancedQueueExecutor) executor);
    }

    public static class EnhancedQueueExecutorAdapter implements ExecutorAdapter<EnhancedQueueExecutor> {

        private final EnhancedQueueExecutor executor;

        public EnhancedQueueExecutorAdapter(EnhancedQueueExecutor executor) {
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
        public long getTaskCount() {
            return getCompletedTaskCount() + getQueueSize() + getActiveCount();
        }

        @Override
        public long getCompletedTaskCount() {
            return this.executor.getCompletedTaskCount();
        }

        @Override
        public String getQueueType() {
            return "TaskNode";
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
        public int getQueueCapacity() {
            return this.executor.getMaximumQueueSize();
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
        public void preStartAllCoreThreads() {
            this.executor.prestartAllCoreThreads();
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
        public String getRejectHandlerType() {
            return this.executor.getHandoffExecutor().getClass().getSimpleName();
        }

        @Override
        public boolean isTerminating() {
            return this.executor.isTerminating();
        }

        @Override
        public boolean isTerminated() {
            return this.executor.isTerminated();
        }

        @Override
        public boolean isShutdown() {
            return this.executor.isShutdown();
        }
    }
}
