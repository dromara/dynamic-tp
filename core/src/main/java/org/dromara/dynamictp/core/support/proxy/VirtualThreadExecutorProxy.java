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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.aware.RejectHandlerAware;
import org.dromara.dynamictp.core.aware.TaskEnhanceAware;
import org.dromara.dynamictp.core.support.task.runnable.EnhancedRunnable;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * Proxy for virtual-thread-per-task executors. It wraps an {@link ExecutorService}
 * delegate that may be either a JDK {@code ThreadPerTaskExecutor} (created by
 * {@link org.dromara.dynamictp.core.executor.VirtualThreadExecutorFactory}) or an
 * adapted {@code SimpleAsyncTaskExecutor} (Spring Boot 3 virtual threads).
 *
 * <p>The proxy deliberately implements only {@link ExecutorService},
 * {@link TaskEnhanceAware} and {@link RejectHandlerAware} (all already part of the
 * dtp aware hierarchy). It does NOT implement {@link org.dromara.dynamictp.core.support.adapter.ExecutorAdapter}
 * directly, because {@code ExecutorService} and {@code ExecutorAdapter} both declare
 * {@code isShutdown} / {@code isTerminated} / {@code execute} and the JVM would reject
 * the unrelated default-method conflict. Instead {@link org.dromara.dynamictp.core.support.VirtualThreadExecutorAdapter}
 * adapts this proxy and exposes the {@code ExecutorAdapter} view, while keeping a
 * reference to the proxy (not the bare delegate) so task-wrappers / aware / notify
 * state stay reachable.</p>
 *
 * <p>Key design points versus the earlier prototype:</p>
 * <ul>
 *   <li>{@link AbstractExecutorService}'s submit / invokeAll / invokeAny methods all
 *       delegate to {@link #execute(Runnable)}, so every entry point goes through the
 *       same enhancement path.</li>
 *   <li>Size/queue metrics return {@code -1} (unsupported). Virtual threads have no
 *       bounded pool or queue; performance metrics (tps/rt/reject) still flow through
 *       the aware chain.</li>
 *   <li>Task statistics and the aware {@code execute} hook are registered when a task
 *       starts running, not when it is submitted, so a task that is rejected after
 *       being decorated leaves no residue behind.</li>
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
     * Cumulative number of tasks that started running, rejected ones excluded.
     * The JDK thread-per-task executor keeps no such counter (and its internal
     * {@code threadCount()} is not reachable without opening {@code java.util.concurrent}),
     * so the numbers are tracked here, where every submission path passes through.
     */
    private final LongAdder taskCount = new LongAdder();

    /**
     * Cumulative number of finished tasks, including the ones that threw.
     */
    private final LongAdder completedTaskCount = new LongAdder();

    /**
     * Tasks currently running, which equals the number of live threads for a
     * thread-per-task executor.
     */
    private final AtomicInteger activeCount = new AtomicInteger();

    /**
     * High-water mark of {@link #activeCount}.
     */
    private final AtomicInteger largestActiveCount = new AtomicInteger();

    /**
     * Task wrappers, do sth enhanced.
     */
    private List<TaskWrapper> taskWrappers;

    /**
     * Task wrapper that dtp created itself while taking over the executor, currently the
     * adapted {@code TaskDecorator} of a Spring {@code SimpleAsyncTaskExecutor} whose
     * decorator slot dtp had to occupy. It is not derived from {@code taskWrapperNames},
     * so a config refresh must not drop it, see {@link #setTaskWrappers(List)}.
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
            // The delegate refused the task (e.g. RejectedExecutionException from a JDK
            // thread-per-task executor, or TaskRejectedException from a SimpleAsyncTaskExecutor
            // that is closed / has reached its concurrency limit).
            // Task statistics and the aware "execute" hook are registered when the task starts
            // running (see decorate), so nothing has to be rolled back here, but the rejection
            // itself still has to be reported.
            // Note: the aware chain is keyed on the inner task (see decorate), not on the
            // outer EnhancedRunnable.
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
     * <p>Key must stay consistent with {@link EnhancedRunnable}:
     * {@link AwareManager#execute} uses the same runnable instance that
     * {@code beforeExecute}/{@code afterExecute} later see (the inner task),
     * matching the Undertow proxy pattern. Otherwise queue-timeout timers
     * and performance metrics cannot cancel/complete correctly.</p>
     *
     * <p><b>No side effect on purpose</b>: task counting and the aware {@code execute}
     * hook happen when the returned task actually starts (see
     * {@link CountingEnhancedRunnable}). This method is also installed as the
     * {@code TaskDecorator} of a Spring {@code SimpleAsyncTaskExecutor}, whose
     * {@code execute} may still reject the task <i>after</i> decorating it (closed
     * executor, concurrency limit, thread creation failure). A decorator cannot observe
     * that rejection, so registering statistics here would permanently leak the active
     * count and the performance stopwatch entry of a task that never ran.</p>
     *
     * <p>Idempotent: if the command is already an {@link EnhancedRunnable}
     * produced by a previous {@code decorate} (e.g. SimpleAsyncTaskExecutor
     * TaskDecorator + registry {@code execute} double entry), return it as-is
     * so wrappers / aware hooks are not applied twice.</p>
     */
    public Runnable decorate(Runnable command) {
        if (command instanceof EnhancedRunnable) {
            return command;
        }
        // Keep the same key for execute / beforeExecute / afterExecute (Undertow style).
        Runnable enhanced = getEnhancedTask(command);
        return new CountingEnhancedRunnable(enhanced, this);
    }

    // ---------------------------------------------------------------------
    // Task statistics, see VirtualThreadExecutorAdapter for the ExecutorAdapter view
    // ---------------------------------------------------------------------

    /**
     * Cumulative number of tasks that started running, rejected ones excluded. Unlike
     * {@link java.util.concurrent.ThreadPoolExecutor#getTaskCount()} it does not include tasks
     * that were accepted but have not started yet: a thread-per-task executor has no queue to
     * count them in, and counting at submission time cannot be undone when the underlying
     * Spring executor rejects an already decorated task.
     *
     * @return the task count
     */
    public long getTaskCount() {
        return taskCount.sum();
    }

    /**
     * @return cumulative number of finished tasks, including the ones that threw
     */
    public long getCompletedTaskCount() {
        return completedTaskCount.sum();
    }

    /**
     * @return tasks currently running, i.e. live threads for a thread-per-task executor
     */
    public int getActiveCount() {
        return activeCount.get();
    }

    /**
     * @return high-water mark of {@link #getActiveCount()}
     */
    public int getLargestActiveCount() {
        return largestActiveCount.get();
    }

    /**
     * Called when a task actually starts, which for a thread-per-task executor is also the
     * moment its thread comes to life. A task that ends up rejected therefore never shows up
     * in any counter, and never leaves an entry behind in the aware chain.
     *
     * @param awareKey the inner task, i.e. the key shared with beforeExecute / afterExecute
     */
    private void taskStarted(Runnable awareKey) {
        taskCount.increment();
        int active = activeCount.incrementAndGet();
        largestActiveCount.accumulateAndGet(active, Math::max);
        AwareManager.execute(this, awareKey);
    }

    /**
     * Active count is decremented first, so an observer that sees the completion also sees the
     * thread gone (otherwise metrics and tests can catch a moment where a completed task is
     * still reported as active).
     */
    private void taskCompleted() {
        activeCount.decrementAndGet();
        completedTaskCount.increment();
    }

    /**
     * Counts task execution. Extending {@link EnhancedRunnable} keeps {@link #decorate}
     * idempotent (its check is {@code instanceof EnhancedRunnable}) and keeps the aware
     * hooks of the parent class intact.
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
     * <p>A config refresh always overwrites the wrappers with the ones resolved from
     * {@code taskWrapperNames} (empty when nothing is configured). The internal wrapper
     * created at registration time (see {@link #setInternalTaskWrapper(TaskWrapper)}) is
     * merged back in, otherwise the first config change would silently and permanently
     * drop the {@code TaskDecorator} that the underlying Spring executor was configured
     * with, as dtp replaced its decorator slot.</p>
     */
    @Override
    public void setTaskWrappers(List<TaskWrapper> taskWrappers) {
        if (Objects.isNull(internalTaskWrapper)) {
            this.taskWrappers = taskWrappers;
            return;
        }
        // the internal wrapper stays innermost, so configured wrappers (mdc / ttl ...)
        // establish their context around it
        List<TaskWrapper> merged = Lists.newArrayList(internalTaskWrapper);
        if (CollectionUtils.isNotEmpty(taskWrappers)) {
            taskWrappers.stream()
                    .filter(t -> t != internalTaskWrapper)
                    .forEach(merged::add);
        }
        this.taskWrappers = merged;
    }

    /**
     * Set the wrapper dtp created itself while taking over the executor. It is kept across
     * config refreshes.
     *
     * @param internalTaskWrapper the internal task wrapper
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
