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

package org.dromara.dynamictp.starter.adapter.webserver.adapter.tomcat;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.reject.RejectedInvocationHandler;
import org.dromara.dynamictp.core.support.ExecutorWrapper;

import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * Tomcat ThreadPool Proxy
 *
 * @author kyao
 * @since 1.1.4
 */
@Slf4j
public class TomcatExecutorProxy extends ThreadPoolExecutor {

    public TomcatExecutorProxy(ExecutorWrapper executorWrapper) {
        this((ThreadPoolExecutor) executorWrapper.getExecutor().getOriginal());
        executorWrapper.setOriginalProxy(this);

        RejectedExecutionHandler handler = getRejectedExecutionHandler();
        setRejectedExecutionHandler((RejectedExecutionHandler) Proxy
                .newProxyInstance(handler.getClass().getClassLoader(),
                        new Class[]{RejectedExecutionHandler.class},
                        new RejectedInvocationHandler(handler)));
    }

    private TomcatExecutorProxy(ThreadPoolExecutor executor) {
        super(executor.getCorePoolSize(), executor.getMaximumPoolSize(),
                executor.getKeepAliveTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS, executor.getQueue(),
                executor.getThreadFactory(), executor.getRejectedExecutionHandler());
    }

    @Override
    public void execute(Runnable command) {
        AwareManager.executeEnhance(this, command);
        super.execute(command);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        AwareManager.beforeExecuteEnhance(this, t, r);
    }


    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        AwareManager.afterExecuteEnhance(this, r, t);
    }

}
