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

package org.dromara.dynamictp.core.support.proxy;

import com.google.common.collect.Sets;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.aware.RejectHandlerAware;
import org.dromara.dynamictp.core.aware.TaskEnhanceAware;
import org.dromara.dynamictp.core.support.task.runnable.EnhancedRunnable;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrappers;

import java.util.List;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * Proxy for virtual-thread-per-task executors. The delegate is either a JDK
 * {@code ThreadPerTaskExecutor} (created by
 * {@link org.dromara.dynamictp.core.executor.VirtualThreadExecutorFactory}) or an adapted
 * {@code SimpleAsyncTaskExecutor}.
 *
 * <p>It implements {@link ExecutorService} rather than
 * {@link org.dromara.dynamictp.core.support.adapter.ExecutorAdapter}, because both declare
 * {@code isShutdown} / {@code isTerminated} / {@code execute} and the JVM rejects the unrelated
 * default-method conflict. {@link org.dromara.dynamictp.core.support.VirtualThreadExecutorAdapter}
 * provides the {@code ExecutorAdapter} view over this proxy (not over the bare delegate, so
 * task-wrappers / aware / notify state stay reachable).</p>
 *
 * <ul>
 *   <li>submit / invokeAll / invokeAny inherited from {@link AbstractExecutorService} all go
 *       through {@link #execute(Runnable)}, so every entry point is enhanced the same way.</li>
 *   <li>Pool and queue shape metrics are not applicable, see the adapter. Concurrency and task
 *       metrics are real.</li>
 *   <li>Statistics and the aware {@code execute} hook are registered when a task starts, not when
 *       it is submitted, so a task rejected after being decorated leaves no residue.</li>
 * </ul>
 *
 * @author yanhom
 * @since 1.3.0
 */
public class VirtualThreadExecutorProxy extends AbstractExecutorService
        implements TaskEnhanceAware, RejectHandlerAware {

    private final ExecutorService delegate;

    /**
     * Guards the one-shot {@code terminated} aware notification.
     */
    private final AtomicBoolean terminatedNotified = new AtomicBoolean(false);

    /**
     * Cumulative number of tasks that started running, rejected ones excluded. Monotonically
     * increasing, and {@code >= completedTaskCount + activeCount} at any time. The JDK
     * thread-per-task executor exposes no such counter, hence tracked here.
     */
    private final LongAdder startedTaskCount = new LongAdder();

    /**
     * Cumulative number of finished tasks, including the ones that threw.
     */
    private final LongAdder completedTaskCount = new LongAdder();

    /**
     * Tasks currently running, i.e. the concurrency the executor currently sustains.
     */
    private final AtomicInteger activeCount = new AtomicInteger();

    /**
     * Task wrappers, do sth enhanced.
     */
    private List<TaskWrapper> taskWrappers;

    /**
     * Wrapper dtp created itself while taking over the executor, currently the adapted
     * {@code TaskDecorator} of the wrapped Spring executor. Not derived from
     * {@code taskWrapperNames}, so {@link #setTaskWrappers(List)} keeps it across refreshes.
     */
    private TaskWrapper internalTaskWrapper;

    /**
     * Reject handler type.
     */
    private String rejectHandlerType = "unknown";

    /**
     * The name of the thread pool.
     */
    private String threadPoolName;

    /**
     * Simple Business alias name of Dynamic ThreadPool. Use for notify.
     */
    private String threadPoolAliasName;

    /**
     * If enable notify.
     */
    private boolean notifyEnabled = true;

    /**
     * If enhance reject.
     */
    private boolean rejectEnhanced = true;

    /**
     * Whether to wait for tasks to complete on shutdown.
     */
    private boolean waitForTasksToCompleteOnShutdown = false;

    /**
     * Await termination seconds.
     */
    private int awaitTerminationSeconds = 0;

    /**
     * Task execute timeout, unit (ms).
     */
    private long runTimeout = 0;

    /**
     * Try interrupt task when timeout.
     */
    private boolean tryInterrupt = false;

    /**
     * Task queue wait timeout, unit (ms).
     */
    private long queueTimeout = 0;

    /**
     * Notify items, see {@link NotifyItemEnum}.
     */
    private List<NotifyItem> notifyItems;

    /**
     * Notify platform ids.
     */
    private List<String> platformIds;

    /**
     * Plugin names.
     */
    private Set<String> pluginNames = Sets.newHashSet();

    /**
     * Aware names.
     */
    private Set<String> awareNames = Sets.newHashSet();

    public VirtualThreadExecutorProxy(ExecutorService delegate) {
        this.delegate = delegate;
    }

    public ExecutorService getDelegate() {
        return delegate;
    }

    // ---------------------------------------------------------------------
    // ExecutorService facade: every entry point funnels through decorate
    // ---------------------------------------------------------------------

    @Override
    public void execute(Runnable command) {
        Runnable enhanced = decorate(command);
        try {
            delegate.execute(enhanced);
        } catch (RuntimeException e) {
            // Delegate refused the task. Statistics and the aware "execute" hook are registered
            // when a task starts (see decorate), so there is nothing to roll back, but the
            // rejection must still be reported, keyed on the inner task like before/afterExecute.
            Runnable awareKey = enhanced instanceof EnhancedRunnable
                    ? ((EnhancedRunnable) enhanced).getRunnable() : enhanced;
            AwareManager.beforeReject(awareKey, this);
            AwareManager.afterReject(awareKey, this);
            throw e;
        }
    }

    @Override
    public void shutdown() {
        AwareManager.shutdown(this);
        delegate.shutdown();
        tryNotifyTerminated();
    }

    @Override
    public List<Runnable> shutdownNow() {
        List<Runnable> tasks = delegate.shutdownNow();
        AwareManager.shutdownNow(this, tasks);
        tryNotifyTerminated();
        return tasks;
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        boolean terminated = delegate.awaitTermination(timeout, unit);
        if (terminated) {
            tryNotifyTerminated();
        }
        return terminated;
    }

    /**
     * There is no {@code terminated()} hook on {@link AbstractExecutorService}, so the
     * aware chain is notified from the shutdown / awaitTermination paths instead, at most
     * once (mirroring {@link java.util.concurrent.ThreadPoolExecutor#terminated()}).
     */
    private void tryNotifyTerminated() {
        if (delegate.isTerminated() && terminatedNotified.compareAndSet(false, true)) {
            AwareManager.terminated(this);
        }
    }

    // ---------------------------------------------------------------------
    // Enhancement helper: single source of truth for all submit/execute paths
    // ---------------------------------------------------------------------

    /**
     * Enhance a task once, without any bookkeeping side effect.
     *
     * <p>Counting and the aware {@code execute} hook happen when the returned task starts, see
     * {@link CountingEnhancedRunnable}. This method is also installed as the {@code TaskDecorator}
     * of a Spring {@code SimpleAsyncTaskExecutor}, which may reject a task <i>after</i> decorating
     * it; a decorator cannot observe that, so registering anything here would leak.</p>
     *
     * <p>Idempotent, since a task can reach this method twice (bean decorator plus registry
     * {@code execute}), and wrappers / aware hooks must not be applied twice.</p>
     */
    public Runnable decorate(Runnable command) {
        if (command instanceof EnhancedRunnable) {
            return command;
        }
        // the inner task is the key shared by execute / beforeExecute / afterExecute, so that
        // timeout timers and performance metrics can be cancelled / completed (Undertow style)
        Runnable enhanced = getEnhancedTask(command);
        return new CountingEnhancedRunnable(enhanced, this);
    }

    // ---------------------------------------------------------------------
    // Task statistics, see VirtualThreadExecutorAdapter for the ExecutorAdapter view
    // ---------------------------------------------------------------------

    /**
     * Cumulative number of tasks that started running, rejected ones excluded. Unlike
     * {@link java.util.concurrent.ThreadPoolExecutor#getTaskCount()} it excludes tasks accepted
     * but not started yet: there is no queue to count them in, and counting at submission time
     * cannot be undone when the Spring executor rejects an already decorated task.
     *
     * @return the task count
     */
    public long getTaskCount() {
        return startedTaskCount.sum();
    }

    /**
     * @return cumulative number of finished tasks, including the ones that threw
     */
    public long getCompletedTaskCount() {
        return completedTaskCount.sum();
    }

    /**
     * @return tasks currently running, i.e. the current concurrency level
     */
    public int getActiveCount() {
        return activeCount.get();
    }

    /**
     * Called when a task actually starts, so a rejected task shows up in no counter and leaves
     * nothing behind in the aware chain.
     *
     * @param awareKey the inner task, shared with beforeExecute / afterExecute
     */
    private void taskStarted(Runnable awareKey) {
        startedTaskCount.increment();
        activeCount.incrementAndGet();
        AwareManager.execute(this, awareKey);
    }

    /**
     * The active count is decremented first, so an observer seeing the completion also sees the
     * task gone.
     */
    private void taskCompleted() {
        activeCount.decrementAndGet();
        completedTaskCount.increment();
    }

    /**
     * Counts task execution. Extending {@link EnhancedRunnable} keeps {@link #decorate} idempotent
     * and the parent's aware hooks intact.
     */
    private static class CountingEnhancedRunnable extends EnhancedRunnable {

        private final VirtualThreadExecutorProxy proxy;

        CountingEnhancedRunnable(Runnable runnable, VirtualThreadExecutorProxy proxy) {
            super(runnable, proxy);
            this.proxy = proxy;
        }

        @Override
        public void run() {
            proxy.taskStarted(getRunnable());
            try {
                super.run();
            } finally {
                proxy.taskCompleted();
            }
        }
    }

    // ---------------------------------------------------------------------
    // TaskEnhanceAware
    // ---------------------------------------------------------------------

    @Override
    public List<TaskWrapper> getTaskWrappers() {
        return taskWrappers;
    }

    /**
     * {@inheritDoc}
     *
     * <p>{@link #internalTaskWrapper} is merged back in, see
     * {@link TaskWrappers#merge(TaskWrapper, List)}.</p>
     */
    @Override
    public void setTaskWrappers(List<TaskWrapper> taskWrappers) {
        this.taskWrappers = TaskWrappers.merge(internalTaskWrapper, taskWrappers);
    }

    /**
     * @param internalTaskWrapper wrapper dtp created itself, kept across config refreshes
     */
    public void setInternalTaskWrapper(TaskWrapper internalTaskWrapper) {
        this.internalTaskWrapper = internalTaskWrapper;
        setTaskWrappers(this.taskWrappers);
    }

    public TaskWrapper getInternalTaskWrapper() {
        return internalTaskWrapper;
    }

    // ---------------------------------------------------------------------
    // RejectHandlerAware
    // ---------------------------------------------------------------------

    @Override
    public String getRejectHandlerType() {
        return rejectHandlerType;
    }

    @Override
    public void setRejectHandlerType(String rejectHandlerType) {
        this.rejectHandlerType = rejectHandlerType;
    }

    // ---------------------------------------------------------------------
    // Pool metadata used by notify / converter / config refresh
    // ---------------------------------------------------------------------

    public String getThreadPoolName() {
        return threadPoolName;
    }

    public void setThreadPoolName(String threadPoolName) {
        this.threadPoolName = threadPoolName;
    }

    public String getThreadPoolAliasName() {
        return threadPoolAliasName;
    }

    public void setThreadPoolAliasName(String threadPoolAliasName) {
        this.threadPoolAliasName = threadPoolAliasName;
    }

    public boolean isNotifyEnabled() {
        return notifyEnabled;
    }

    public void setNotifyEnabled(boolean notifyEnabled) {
        this.notifyEnabled = notifyEnabled;
    }

    public boolean isRejectEnhanced() {
        return rejectEnhanced;
    }

    public void setRejectEnhanced(boolean rejectEnhanced) {
        this.rejectEnhanced = rejectEnhanced;
    }

    public boolean isWaitForTasksToCompleteOnShutdown() {
        return waitForTasksToCompleteOnShutdown;
    }

    public void setWaitForTasksToCompleteOnShutdown(boolean waitForTasksToCompleteOnShutdown) {
        this.waitForTasksToCompleteOnShutdown = waitForTasksToCompleteOnShutdown;
    }

    public int getAwaitTerminationSeconds() {
        return awaitTerminationSeconds;
    }

    public void setAwaitTerminationSeconds(int awaitTerminationSeconds) {
        this.awaitTerminationSeconds = awaitTerminationSeconds;
    }

    public long getRunTimeout() {
        return runTimeout;
    }

    public void setRunTimeout(long runTimeout) {
        this.runTimeout = runTimeout;
    }

    public boolean isTryInterrupt() {
        return tryInterrupt;
    }

    public void setTryInterrupt(boolean tryInterrupt) {
        this.tryInterrupt = tryInterrupt;
    }

    public long getQueueTimeout() {
        return queueTimeout;
    }

    public void setQueueTimeout(long queueTimeout) {
        this.queueTimeout = queueTimeout;
    }

    public List<NotifyItem> getNotifyItems() {
        return notifyItems;
    }

    public void setNotifyItems(List<NotifyItem> notifyItems) {
        this.notifyItems = notifyItems;
    }

    public List<String> getPlatformIds() {
        return platformIds;
    }

    public void setPlatformIds(List<String> platformIds) {
        this.platformIds = platformIds;
    }

    public Set<String> getPluginNames() {
        return pluginNames;
    }

    public void setPluginNames(Set<String> pluginNames) {
        this.pluginNames = pluginNames;
    }

    public Set<String> getAwareNames() {
        return awareNames;
    }

    public void setAwareNames(Set<String> awareNames) {
        this.awareNames = awareNames;
    }
}
