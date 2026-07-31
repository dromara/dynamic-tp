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

import org.dromara.dynamictp.core.support.adapter.ExecutorAdapter;
import org.dromara.dynamictp.core.support.proxy.VirtualThreadExecutorProxy;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;

/**
 * {@link ExecutorAdapter} view over a {@link VirtualThreadExecutorProxy}.
 *
 * <p>Unlike the old prototype (which unwrapped the proxy and stored the bare
 * delegate, silently dropping task-wrappers / aware / notify state), this adapter
 * keeps a reference to the proxy itself. {@code getOriginal()} returns the proxy,
 * so {@code ExecutorWrapper#setTaskWrappers} / {@code setRejectHandler} keep working
 * via the {@code TaskEnhanceAware} / {@code RejectHandlerAware} contracts.</p>
 *
 * <p>Virtual threads are unbounded and have no queue, so sizing / queue / keepAlive
 * metrics return {@code -1} (the {@code unsupported} convention already used by
 * {@link ExecutorAdapter}'s default methods). Task statistics (task count, completed
 * task count, active count, largest pool size) are tracked by the proxy itself, since
 * the JDK thread-per-task executor exposes no counters.</p>
 *
 * @author yanhom
 * @since 1.3.0
 */
public class VirtualThreadExecutorAdapter implements ExecutorAdapter<VirtualThreadExecutorProxy> {

    /**
     * Queue type shown in metrics / notifications, virtual threads have no queue.
     */
    public static final String QUEUE_TYPE = "NoQueue(VirtualThread)";

    private final VirtualThreadExecutorProxy proxy;

    public VirtualThreadExecutorAdapter(VirtualThreadExecutorProxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public VirtualThreadExecutorProxy getOriginal() {
        return proxy;
    }

    @Override
    public void execute(Runnable command) {
        proxy.execute(command);
    }

    @Override
    public int getCorePoolSize() {
        return -1;
    }

    @Override
    public void setCorePoolSize(int corePoolSize) {
        // unsupported: virtual threads are unbounded
    }

    @Override
    public int getMaximumPoolSize() {
        return -1;
    }

    @Override
    public void setMaximumPoolSize(int maximumPoolSize) {
        // unsupported: virtual threads are unbounded
    }

    @Override
    public int getPoolSize() {
        // thread-per-task: one live thread per running task
        return proxy.getActiveCount();
    }

    @Override
    public int getActiveCount() {
        return proxy.getActiveCount();
    }

    @Override
    public int getLargestPoolSize() {
        return proxy.getLargestActiveCount();
    }

    @Override
    public long getTaskCount() {
        return proxy.getTaskCount();
    }

    @Override
    public long getCompletedTaskCount() {
        return proxy.getCompletedTaskCount();
    }

    @Override
    public boolean isShutdown() {
        return proxy.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return proxy.isTerminated();
    }

    @Override
    public String getRejectHandlerType() {
        return proxy.getRejectHandlerType();
    }

    @Override
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        // Virtual threads never reject (unbounded). Reject handler type is
        // already set on the proxy via RejectHandlerAware by ExecutorWrapper
        // (using the original handler's simple name). Do NOT overwrite it here
        // with RejectHandlerGetter proxy names such as "$Proxy14".
    }

    @Override
    public String getQueueType() {
        // Thread-per-task: there is no queue at all. Reporting the internal
        // "UnsupportedBlockingQueue" class name would be misleading in metrics,
        // registration logs and config-change notifications.
        return QUEUE_TYPE;
    }

    @Override
    public long getKeepAliveTime(TimeUnit unit) {
        return -1;
    }

    @Override
    public void setKeepAliveTime(long time, TimeUnit unit) {
        // unsupported
    }

    @Override
    public boolean allowsCoreThreadTimeOut() {
        return false;
    }

    @Override
    public void allowCoreThreadTimeOut(boolean value) {
        // unsupported
    }
}
