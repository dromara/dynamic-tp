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

package org.dromara.dynamictp.adapter.dubbo.apache;

import org.apache.dubbo.common.threadpool.support.eager.EagerThreadPoolExecutor;
import org.apache.dubbo.common.threadpool.support.eager.TaskQueue;
import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.aware.RejectHandlerAware;
import org.dromara.dynamictp.core.aware.TaskEnhanceAware;
import org.dromara.dynamictp.core.reject.RejectHandlerGetter;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * EagerThreadPoolExecutorProxy related
 *
 * @author yanhom
 * @since 1.1.5
 */
public class EagerThreadPoolExecutorProxy extends EagerThreadPoolExecutor implements TaskEnhanceAware, RejectHandlerAware {

    /**
     * Task wrappers, do sth enhanced.
     */
    private List<TaskWrapper> taskWrappers;

    private final String rejectHandlerType;

    public EagerThreadPoolExecutorProxy(EagerThreadPoolExecutor executor) {
        super(executor.getCorePoolSize(), executor.getMaximumPoolSize(),
                executor.getKeepAliveTime(TimeUnit.MILLISECONDS),
                TimeUnit.MILLISECONDS, (TaskQueue<Runnable>) executor.getQueue(),
                executor.getThreadFactory(), executor.getRejectedExecutionHandler());
        this.rejectHandlerType = getRejectedExecutionHandler().getClass().getSimpleName();
        setRejectedExecutionHandler(RejectHandlerGetter.getProxy(getRejectedExecutionHandler()));
        ((TaskQueue<Runnable>) getQueue()).setExecutor(this);
        executor.shutdownNow();
    }

    @Override
    public void execute(Runnable command) {
        command = getEnhancedTask(command, taskWrappers);
        AwareManager.execute(this, command);
        super.execute(command);
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
    public void setTaskWrappers(List<TaskWrapper> taskWrappers) {
        this.taskWrappers = taskWrappers;
    }

    @Override
    public String getRejectHandlerType() {
        return rejectHandlerType;
    }
}
