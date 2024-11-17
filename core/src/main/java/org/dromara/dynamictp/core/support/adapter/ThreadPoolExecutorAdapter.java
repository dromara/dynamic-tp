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

package org.dromara.dynamictp.core.support.adapter;

import org.dromara.dynamictp.core.aware.RejectHandlerAware;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ThreadPoolExecutorAdapter implements ExecutorAdapter, the goal of this class
 * is to be compatible with {@link java.util.concurrent.ThreadPoolExecutor}.
 *
 * @author dragon-zhang
 * @since 1.1.3
 **/
public class ThreadPoolExecutorAdapter implements ExecutorAdapter<ThreadPoolExecutor> {
    
    private final ThreadPoolExecutor executor;
    
    public ThreadPoolExecutorAdapter(ThreadPoolExecutor executor) {
        this.executor = executor;
    }
    
    @Override
    public ThreadPoolExecutor getOriginal() {
        return this.executor;
    }
    
    @Override
    public void execute(Runnable command) {
        this.executor.execute(command);
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
        return this.executor.getTaskCount();
    }
    
    @Override
    public long getCompletedTaskCount() {
        return this.executor.getCompletedTaskCount();
    }
    
    @Override
    public BlockingQueue<Runnable> getQueue() {
        return this.executor.getQueue();
    }

    @Override
    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return this.executor.getRejectedExecutionHandler();
    }

    @Override
    public String getRejectHandlerType() {
        if (executor instanceof RejectHandlerAware) {
            return ((RejectHandlerAware) executor).getRejectHandlerType();
        }
        return getRejectedExecutionHandler().getClass().getSimpleName();
    }

    @Override
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        this.executor.setRejectedExecutionHandler(handler);
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
        return this.executor.getKeepAliveTime(unit);
    }
    
    @Override
    public void setKeepAliveTime(long time, TimeUnit unit) {
        this.executor.setKeepAliveTime(time, unit);
    }

    @Override
    public boolean isShutdown() {
        return this.executor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return this.executor.isTerminated();
    }

    @Override
    public boolean isTerminating() {
        return this.executor.isTerminating();
    }
}
