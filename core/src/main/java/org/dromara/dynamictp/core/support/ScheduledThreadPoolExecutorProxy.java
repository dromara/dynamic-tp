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

package org.dromara.dynamictp.core.support;

import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.aware.RejectHandlerAware;
import org.dromara.dynamictp.core.aware.TaskEnhanceAware;
import org.dromara.dynamictp.core.reject.RejectHandlerGetter;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ScheduledThreadPoolExecutorProxy related
 * The schedule method does not support queue timeout monitoring
 *
 * @author kyao
 * @since 1.1.5
 */
public class ScheduledThreadPoolExecutorProxy extends ScheduledThreadPoolExecutor implements TaskEnhanceAware, RejectHandlerAware {

    /**
     * Task wrappers, do sth enhanced.
     */
    private List<TaskWrapper> taskWrappers;

    /**
     * Reject handler type.
     */
    private final String rejectHandlerType;

    public ScheduledThreadPoolExecutorProxy(ScheduledThreadPoolExecutor executor) {
        super(executor.getCorePoolSize(), executor.getThreadFactory());
        this.rejectHandlerType = executor.getRejectedExecutionHandler().getClass().getSimpleName();
        setRejectedExecutionHandler(RejectHandlerGetter.getProxy(getRejectedExecutionHandler()));
    }

    @Override
    public void execute(Runnable command) {
        command = getEnhancedTask(command);
        AwareManager.execute(this, command);
        super.execute(command);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        command = getEnhancedTask(command);
        AwareManager.execute(this, command);
        return super.schedule(command, delay, unit);
    }

    public <V> ScheduledFuture<V> schedule(Runnable command, V result, long delay, TimeUnit unit) {
        command = getEnhancedTask(command);
        AwareManager.execute(this, command);
        return super.schedule(Executors.callable(command, result), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        command = getEnhancedTask(command);
        AwareManager.execute(this, command);
        return super.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        command = getEnhancedTask(command);
        AwareManager.execute(this, command);
        return super.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        AwareManager.beforeExecute(this, t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        AwareManager.afterExecute(this, r, t);
    }

    @Override
    public String getRejectHandlerType() {
        return rejectHandlerType;
    }

    @Override
    public List<TaskWrapper> getTaskWrappers() {
        return taskWrappers;
    }

    @Override
    public void setTaskWrappers(List<TaskWrapper> taskWrappers) {
        this.taskWrappers = taskWrappers;
    }
}
