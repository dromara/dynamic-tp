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
 * queue, so pool-shape numbers cannot mean what they mean on a {@code ThreadPoolExecutor} and
 * are reported as {@value #NOT_APPLICABLE} rather than as a number that would be read as a
 * healthy pool. Task and concurrency numbers on the other hand are real and are reported:</p>
 * <table border="1">
 *   <caption>metric mapping</caption>
 *   <tr><th>metric</th><th>value</th></tr>
 *   <tr><td>corePoolSize / maximumPoolSize / keepAliveTime</td>
 *       <td>{@value #NOT_APPLICABLE}, the executor is unbounded</td></tr>
 *   <tr><td>queueSize / queueCapacity / queueRemainingCapacity</td>
 *       <td>{@value #NOT_APPLICABLE}, there is no queue. Reporting 0 would read as "queue is
 *       empty", which is not the same statement</td></tr>
 *   <tr><td>poolSize / largestPoolSize</td>
 *       <td>{@value #NOT_APPLICABLE}, threads are created and discarded per task: there is no
 *       pool to size, and no persistent worker peak that sampling could not reconstruct from
 *       activeCount</td></tr>
 *   <tr><td>activeCount</td><td>tasks currently running, i.e. the concurrency level right
 *       now</td></tr>
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

    /**
     * {@inheritDoc}
     *
     * <p>{@link #NOT_APPLICABLE}: threads are created and discarded per task, so there is no
     * pool whose size could be reported. The concurrency currently sustained is
     * {@link #getActiveCount()}.</p>
     */
    @Override
    public int getPoolSize() {
        return NOT_APPLICABLE;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Tasks currently running, i.e. the concurrency level the executor sustains right now.
     * This is a real number, not a pool property.</p>
     */
    @Override
    public int getActiveCount() {
        return proxy.getActiveCount();
    }

    /**
     * {@inheritDoc}
     *
     * <p>{@link #NOT_APPLICABLE}: on a {@code ThreadPoolExecutor} this records the peak number of
     * persistent workers, which sampling cannot reconstruct. A thread-per-task executor has no
     * persistent workers, so the peak is just the maximum of {@link #getActiveCount()} over time
     * and tracking it separately would only duplicate that series.</p>
     */
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
