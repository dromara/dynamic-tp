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
 * {@link ExecutorAdapter} view over a {@link VirtualThreadExecutorProxy}. {@code getOriginal()}
 * returns the proxy, not the bare delegate, so {@code ExecutorWrapper#setTaskWrappers} /
 * {@code setRejectHandler} keep working through the aware contracts.
 *
 * <p>A thread-per-task executor has no bounded pool and no queue, so those metrics report
 * {@value #NOT_APPLICABLE} instead of a number that would read as a healthy pool (reporting 0 for
 * the queue would mean "empty queue", which is a different statement). Reported as real numbers:
 * {@code activeCount} (tasks running right now), {@code taskCount} (tasks <b>started</b>, see
 * {@link VirtualThreadExecutorProxy#getTaskCount()}) and {@code completedTaskCount}.
 * {@code largestPoolSize} is not applicable either: without persistent workers the peak is just
 * max(activeCount) over time, which monitoring already has.</p>
 *
 * @author yanhom
 * @since 1.3.0
 */
public class VirtualThreadExecutorAdapter implements ExecutorAdapter<VirtualThreadExecutorProxy> {

    /**
     * Reported for metrics that have no meaning here, following {@link ExecutorAdapter}'s
     * defaults.
     */
    public static final int NOT_APPLICABLE = -1;

    /**
     * Queue type shown in metrics / notifications.
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
        return NOT_APPLICABLE;
    }

    @Override
    public void setCorePoolSize(int corePoolSize) {
        // unsupported: virtual threads are unbounded
    }

    @Override
    public int getMaximumPoolSize() {
        return NOT_APPLICABLE;
    }

    @Override
    public void setMaximumPoolSize(int maximumPoolSize) {
        // unsupported: virtual threads are unbounded
    }

    @Override
    public int getPoolSize() {
        return NOT_APPLICABLE;
    }

    @Override
    public int getActiveCount() {
        return proxy.getActiveCount();
    }

    @Override
    public int getLargestPoolSize() {
        return NOT_APPLICABLE;
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
        // The type is already set on the proxy by ExecutorWrapper via RejectHandlerAware, using
        // the original handler's simple name. Do not overwrite it with a "$Proxy14" style name.
    }

    @Override
    public String getQueueType() {
        // reporting the internal "UnsupportedBlockingQueue" would be misleading
        return QUEUE_TYPE;
    }

    /**
     * {@inheritDoc}
     *
     * <p>{@link #NOT_APPLICABLE} rather than the 0 of {@link ExecutorAdapter}'s default, which is
     * backed by an unsupported queue.</p>
     */
    @Override
    public int getQueueSize() {
        return NOT_APPLICABLE;
    }

    @Override
    public int getQueueCapacity() {
        // must be overridden as well, the default sums size + remainingCapacity
        return NOT_APPLICABLE;
    }

    @Override
    public int getQueueRemainingCapacity() {
        return NOT_APPLICABLE;
    }

    @Override
    public long getKeepAliveTime(TimeUnit unit) {
        return NOT_APPLICABLE;
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
