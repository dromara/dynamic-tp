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
import org.dromara.dynamictp.core.aware.TaskEnhanceAware;
import org.dromara.dynamictp.core.support.task.runnable.EnhancedRunnable;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ClassName: VirtualThreadExecutorProxy
 * Package: org.dromara.dynamictp.core.support
 * Description:
 * VirtualThreadExecutor Proxy
 *
 * @Author CYC
 * @Create 2024/10/14 15:59
 * @Version 1.0
 */
public class VirtualThreadExecutorProxy implements TaskEnhanceAware, ExecutorService {

    private final ExecutorService threadPerTaskExecutor;

    /**
     * Notify platform ids.
     */
    private List<String> platformIds;

    /**
     * Task wrappers, do sth enhanced.
     */
    private List<TaskWrapper> taskWrappers;

    /**
     * The name of the thread pool.
     */
    protected String threadPoolName;

    /**
     * Simple Business alias Name of Dynamic ThreadPool. Use for notify.
     */
    private String threadPoolAliasName;

    /**
     * If enable notify.
     */
    private boolean notifyEnabled = true;

    /**
     * Notify items, see {@link NotifyItemEnum}.
     */
    private List<NotifyItem> notifyItems;

    /**
     * Plugin names.
     */
    private Set<String> pluginNames = Sets.newHashSet();

    /**
     * Aware names.
     */
    private Set<String> awareNames = Sets.newHashSet();


    public VirtualThreadExecutorProxy(ExecutorService executor) {
        super();
        threadPerTaskExecutor = executor;
    }

    @Override
    public void execute(Runnable command) {
        command = getEnhancedTask(command);
        EnhancedRunnable.of(command, this);
        AwareManager.execute(this, command);
        threadPerTaskExecutor.execute(command);
    }

    public ExecutorService getThreadPerTaskExecutor() {
        return threadPerTaskExecutor;
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
    public void shutdown() {
        threadPerTaskExecutor.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return threadPerTaskExecutor.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return threadPerTaskExecutor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return threadPerTaskExecutor.isTerminated();
    }

    @Override
    public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
        return threadPerTaskExecutor.awaitTermination(l, timeUnit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        FutureTask<T> futureTask = new FutureTask<T>(callable);
        futureTask = (FutureTask<T>) getEnhancedTask(futureTask);
        EnhancedRunnable.of(futureTask, this);
        AwareManager.execute(this, futureTask);
        return threadPerTaskExecutor.submit(callable);
    }


    @Override
    public <T> Future<T> submit(Runnable runnable, T t) {
        runnable = getEnhancedTask(runnable);
        EnhancedRunnable.of(runnable, this);
        AwareManager.execute(this, runnable);
        return threadPerTaskExecutor.submit(runnable, t);
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        runnable = getEnhancedTask(runnable);
        EnhancedRunnable.of(runnable, this);
        AwareManager.execute(this, runnable);
        return threadPerTaskExecutor.submit(runnable);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection) throws InterruptedException {
        return threadPerTaskExecutor.invokeAll(collection);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit) throws InterruptedException {
        return threadPerTaskExecutor.invokeAll(collection, l, timeUnit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> collection) throws InterruptedException, ExecutionException {
        return threadPerTaskExecutor.invokeAny(collection);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        return threadPerTaskExecutor.invokeAny(collection, l, timeUnit);
    }

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

    public List<String> getPlatformIds() {
        return platformIds;
    }

    public void setPlatformIds(List<String> platformIds) {
        this.platformIds = platformIds;
    }

    public List<NotifyItem> getNotifyItems() {
        return notifyItems;
    }

    public void setNotifyItems(List<NotifyItem> notifyItems) {
        this.notifyItems = notifyItems;
    }
}
