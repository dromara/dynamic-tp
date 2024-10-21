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

package org.dromara.dynamictp.starter.adapter.webserver.tomcat;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.threads.TaskQueue;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.dromara.dynamictp.common.util.ExecutorUtil;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.aware.RejectHandlerAware;
import org.dromara.dynamictp.core.aware.TaskEnhanceAware;
import org.dromara.dynamictp.core.reject.RejectedInvocationHandler;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Tomcat ThreadPool Proxy
 *
 * @author kyao
 * @since 1.1.4
 */
@Slf4j
@SuppressWarnings("all")
public class TomcatExecutorProxy extends ThreadPoolExecutor implements TaskEnhanceAware, RejectHandlerAware {

    /**
     * Task wrappers, do sth enhanced.
     */
    private List<TaskWrapper> taskWrappers;

    private final String rejectHandlerType;

    public TomcatExecutorProxy(ThreadPoolExecutor executor) {
        super(executor.getCorePoolSize(), executor.getMaximumPoolSize(),
                executor.getKeepAliveTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS,
                executor.getQueue(), executor.getThreadFactory());
        setThreadRenewalDelay(executor.getThreadRenewalDelay());
        allowCoreThreadTimeOut(executor.allowsCoreThreadTimeOut());
        Object handler = getRejectedExecutionHandler(executor);
        this.rejectHandlerType = handler.getClass().getSimpleName();

        // for different tomcat version
        try {
            setRejectedExecutionHandler((RejectedExecutionHandler) Proxy
                    .newProxyInstance(handler.getClass().getClassLoader(),
                            new Class[]{RejectedExecutionHandler.class},
                            new RejectedInvocationHandler(handler)));
        } catch (Throwable t) {
            ReflectionUtil.setFieldValue("handler", this, Proxy
                    .newProxyInstance(handler.getClass().getClassLoader(),
                            new Class[]{java.util.concurrent.RejectedExecutionHandler.class},
                            new RejectedInvocationHandler(handler)));
        }
        if (executor.getQueue() instanceof TaskQueue) {
            ((TaskQueue) executor.getQueue()).setParent(this);
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

    private Object getRejectedExecutionHandler(ThreadPoolExecutor executor) {
        return ReflectionUtil.getFieldValue("handler", executor);
    }
}
