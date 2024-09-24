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

package org.dromara.dynamictp.starter.adapter.webserver.undertow;

import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.aware.TaskEnhanceAware;
import org.dromara.dynamictp.core.support.task.runnable.EnhancedRunnable;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;
import org.jboss.threads.EnhancedQueueExecutor;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

/**
 * EnhancedQueueExecutorProxy related
 *
 * @author yanhom
 * @since 1.1.4
 **/
@SuppressWarnings("all")
public class EnhancedQueueExecutorProxy extends EnhancedQueueExecutor implements TaskEnhanceAware {

    private List<TaskWrapper> taskWrappers;

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
        allowCoreThreadTimeOut(executor.allowsCoreThreadTimeOut());
    }

    @Override
    public void execute(Runnable runnable) {
        Runnable dtpRunnable = getEnhancedTask(runnable);
        AwareManager.execute(this, dtpRunnable);
        try {
            super.execute(EnhancedRunnable.of(dtpRunnable, this));
        } catch (Throwable e) {
            Throwable[] suppressedExceptions = e.getSuppressed();
            for (Throwable t : suppressedExceptions) {
                if (t instanceof RejectedExecutionException) {
                    AwareManager.beforeReject(dtpRunnable, this);
                    return;
                }
            }
            throw e;
        }
    }

    @Override
    public List<TaskWrapper> getTaskWrappers() {
        return this.taskWrappers;
    }

    @Override
    public void setTaskWrappers(List<TaskWrapper> taskWrappers) {
        this.taskWrappers = taskWrappers;
    }
}
