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
 * <p><b>Metric semantics</b>, a thread-per-task executor has neither a bounded pool nor a
 * queue, so the numbers cannot mean the same thing as on a {@code ThreadPoolExecutor}:</p>
 * <table border="1">
 *   <caption>metric mapping</caption>
 *   <tr><th>metric</th><th>value</th></tr>
 *   <tr><td>corePoolSize / maximumPoolSize / keepAliveTime</td>
 *       <td>{@value #NOT_APPLICABLE}, not applicable (unbounded)</td></tr>
 *   <tr><td>queueSize / queueCapacity / queueRemainingCapacity</td>
 *       <td>{@value #NOT_APPLICABLE}, not applicable (there is no queue). Reporting 0 would
 *       read as "queue is empty", which is not the same statement</td></tr>
 *   <tr><td>poolSize / activeCount</td>
 *       <td>both are the number of tasks currently running: one live thread per running
 *       task, so the two metrics are equal by definition</td></tr>
 *   <tr><td>largestPoolSize</td><td>high-water mark of the above</td></tr>
 *   <tr><td>taskCount</td><td>tasks that <b>started</b> running, see
 *       {@link VirtualThreadExecutorProxy#getTaskCount()}</td></tr>
 *   <tr><td>completedTaskCount</td><td>tasks that finished, exceptions included</td></tr>
 * </table>
 *
 * <p>Task statistics are tracked by the proxy itself, since the JDK thread-per-task executor
 * exposes no counters.</p>
 *
 * @author yanhom
 * @since 1.3.0
 */
public class VirtualThreadExecutorAdapter implements ExecutorAdapter<VirtualThreadExecutorProxy> {

    /**
     * Value reported for metrics that have no meaning for a thread-per-task executor,
     * following the convention of {@link ExecutorAdapter}'s default methods.
     */
    public static final int NOT_APPLICABLE = -1;

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

    /**
     * {@inheritDoc}
     *
     * <p>{@link #NOT_APPLICABLE} instead of the 0 that {@link ExecutorAdapter}'s default
     * (backed by an unsupported queue) would report: there is no queue, which is a different
     * statement from an empty queue. Keeping one convention for every inapplicable metric also
     * avoids alarm / log messages that mix {@code -1} sizing with {@code 0} queue numbers.</p>
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
