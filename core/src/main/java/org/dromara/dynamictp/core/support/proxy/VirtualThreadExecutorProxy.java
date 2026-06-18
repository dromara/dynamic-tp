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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
 *   <li>All submit/execute entry points funnel through a single {@link #decorate}
 *       helper, so the Callable-enhancement bug (building a FutureTask then submitting
 *       the raw Callable) cannot recur.</li>
 *   <li>Size/queue metrics return {@code -1} (unsupported). Virtual threads have no
 *       bounded pool or queue; performance metrics (tps/rt/reject) still flow through
 *       the aware chain.</li>
 * </ul>
 *
 * @author yanhom
 * @since 1.x.x
 */
public class VirtualThreadExecutorProxy implements TaskEnhanceAware, RejectHandlerAware, ExecutorService {

    private final ExecutorService delegate;

    /**
     * Task wrappers, do sth enhanced.
     */
    private List<TaskWrapper> taskWrappers;

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
        delegate.execute(enhanced);
    }

    @Override
    public Future<?> submit(Runnable task) {
        FutureTask<Object> futureTask = new FutureTask<>(decorate(task), null);
        delegate.execute(futureTask);
        return futureTask;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        FutureTask<T> futureTask = new FutureTask<>(decorate(task), result);
        delegate.execute(futureTask);
        return futureTask;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        // Wrap the callable as a FutureTask so it still goes through the same
        // enhancement + aware chain as Runnable, then submit the FutureTask itself
        // (this is exactly what the old prototype failed to do).
        FutureTask<T> futureTask = new FutureTask<>(task);
        Runnable enhanced = decorate(futureTask);
        delegate.execute(enhanced);
        return futureTask;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        List<Future<T>> futures = new ArrayList<>(tasks.size());
        try {
            for (Callable<T> task : tasks) {
                futures.add(submit(task));
            }
            for (Future<T> future : futures) {
                if (!future.isDone()) {
                    try {
                        future.get();
                    } catch (CancellationException | ExecutionException ignored) { }
                }
            }
            return futures;
        } catch (InterruptedException e) {
            futures.forEach(future -> future.cancel(true));
            throw e;
        }
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        if (tasks == null || unit == null) {
            throw new NullPointerException();
        }
        long nanos = unit.toNanos(timeout);
        long deadline = System.nanoTime() + nanos;
        List<Future<T>> futures = new ArrayList<>(tasks.size());
        try {
            for (Callable<T> task : tasks) {
                futures.add(submit(task));
            }
            for (Future<T> future : futures) {
                if (future.isDone()) {
                    continue;
                }
                if (nanos <= 0L) {
                    return futures;
                }
                try {
                    future.get(nanos, TimeUnit.NANOSECONDS);
                } catch (CancellationException | ExecutionException ignored) {
                    // keep collecting remaining futures, same contract as AbstractExecutorService
                } catch (TimeoutException e) {
                    return futures;
                }
                nanos = deadline - System.nanoTime();
            }
            return futures;
        } catch (InterruptedException e) {
            futures.forEach(future -> future.cancel(true));
            throw e;
        } finally {
            for (Future<T> future : futures) {
                if (!future.isDone()) {
                    future.cancel(true);
                }
            }
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        try {
            return doInvokeAny(tasks, false, 0L);
        } catch (TimeoutException impossible) {
            throw new AssertionError(impossible);
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (unit == null) {
            throw new NullPointerException();
        }
        return doInvokeAny(tasks, true, unit.toNanos(timeout));
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
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
        return delegate.awaitTermination(timeout, unit);
    }

    // ---------------------------------------------------------------------
    // Enhancement helper: single source of truth for all submit/execute paths
    // ---------------------------------------------------------------------

    public Runnable decorate(Runnable command) {
        Runnable enhanced = getEnhancedTask(command);
        EnhancedRunnable enhancedRunnable = EnhancedRunnable.of(enhanced, this);
        AwareManager.execute(this, enhancedRunnable);
        return enhancedRunnable;
    }

    private <T> T doInvokeAny(Collection<? extends Callable<T>> tasks, boolean timed, long nanos)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        if (tasks.isEmpty()) {
            throw new IllegalArgumentException();
        }
        ExecutorCompletionService<T> completionService = new ExecutorCompletionService<>(this);
        List<Future<T>> futures = new ArrayList<>(tasks.size());
        long deadline = timed ? System.nanoTime() + nanos : 0L;
        ExecutionException executionException = null;
        try {
            for (Callable<T> task : tasks) {
                futures.add(completionService.submit(task));
            }
            for (int remaining = tasks.size(); remaining > 0; remaining--) {
                Future<T> future;
                if (timed) {
                    if (nanos <= 0L) {
                        throw new TimeoutException();
                    }
                    future = completionService.poll(nanos, TimeUnit.NANOSECONDS);
                    if (future == null) {
                        throw new TimeoutException();
                    }
                    nanos = deadline - System.nanoTime();
                } else {
                    future = completionService.take();
                }
                try {
                    return future.get();
                } catch (ExecutionException e) {
                    executionException = e;
                } catch (RuntimeException e) {
                    executionException = new ExecutionException(e);
                }
            }
            if (executionException == null) {
                executionException = new ExecutionException(null);
            }
            throw executionException;
        } finally {
            futures.forEach(future -> future.cancel(true));
        }
    }

    // ---------------------------------------------------------------------
    // TaskEnhanceAware
    // ---------------------------------------------------------------------

    @Override
    public List<TaskWrapper> getTaskWrappers() {
        return taskWrappers;
    }

    @Override
    public void setTaskWrappers(List<TaskWrapper> taskWrappers) {
        this.taskWrappers = taskWrappers;
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
