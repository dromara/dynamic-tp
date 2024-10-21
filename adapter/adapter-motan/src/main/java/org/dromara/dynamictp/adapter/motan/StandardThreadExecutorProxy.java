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

package org.dromara.dynamictp.adapter.motan;

import com.weibo.api.motan.transport.netty.StandardThreadExecutor;
import org.dromara.dynamictp.common.util.ExecutorUtil;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.aware.RejectHandlerAware;
import org.dromara.dynamictp.core.aware.TaskEnhanceAware;
import org.dromara.dynamictp.core.reject.RejectHandlerGetter;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;

import java.util.List;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;

/**
 * @author hanli
 * @since 1.1.4
 */
public class StandardThreadExecutorProxy extends StandardThreadExecutor implements TaskEnhanceAware, RejectHandlerAware {

    private static final String EXECUTOR_QUEUE = "ExecutorQueue";

    /**
     * Task wrappers, do sth enhanced.
     */
    private List<TaskWrapper> taskWrappers;

    private String rejectHandlerType;

    public StandardThreadExecutorProxy(StandardThreadExecutor executor) {
        super(executor.getCorePoolSize(), executor.getMaximumPoolSize(),
                executor.getKeepAliveTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS,
                executor.getMaxSubmittedTaskCount() - executor.getMaximumPoolSize(),
                executor.getThreadFactory(), executor.getRejectedExecutionHandler());
        allowCoreThreadTimeOut(executor.allowsCoreThreadTimeOut());
        RejectedExecutionHandler handler = getRejectedExecutionHandler();
        this.rejectHandlerType = handler.getClass().getSimpleName();
        setRejectedExecutionHandler(RejectHandlerGetter.getProxy(handler));
        if (EXECUTOR_QUEUE.equals(executor.getQueue().getClass().getSimpleName())) {
            ReflectionUtil.setFieldValue("threadPoolExecutor", executor.getQueue(), this);
        }
    }

    @Override
    public void execute(Runnable command) {
        command = getEnhancedTask(command);
        AwareManager.execute(this, command);
        super.execute(command);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        AwareManager.beforeExecute(this, t, r);
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        AwareManager.afterExecute(this, r, t);
        ExecutorUtil.tryExecAfterExecute(r, t);
    }

    @Override
    public List<TaskWrapper> getTaskWrappers() {
        return taskWrappers;
    }

    @Override
    public void setTaskWrappers(List<TaskWrapper> taskWrappers) {
        this.taskWrappers = taskWrappers;
    }

    @Override
    public String getRejectHandlerType() {
        return rejectHandlerType;
    }

    @Override
    public void setRejectHandlerType(String rejectHandlerType) {
        this.rejectHandlerType = rejectHandlerType;
    }
}
