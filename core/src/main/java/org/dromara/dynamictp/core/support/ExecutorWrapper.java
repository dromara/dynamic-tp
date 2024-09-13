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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Data;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.aware.TaskEnhanceAware;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.notifier.capture.CapturedExecutor;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.notifier.manager.NotifyHelper;
import org.dromara.dynamictp.core.reject.RejectHandlerGetter;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Executor wrapper
 *
 * @author yanhom
 * @since 1.0.3
 **/
@Data
public class ExecutorWrapper {

    /**
     * Executor.
     */
    private ExecutorAdapter<?> executor;

    /**
     * Thread pool name.
     */
    private String threadPoolName;

    /**
     * Thread pool alias name.
     */
    private String threadPoolAliasName;

    /**
     * Whether to enable notification.
     */
    private boolean notifyEnabled = true;

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

    /**
     * Task wrappers, do sth enhanced.
     */
    private List<TaskWrapper> taskWrappers = Lists.newArrayList();

    /**
     * If pre start all core threads.
     */
    private boolean preStartAllCoreThreads;

    /**
     * RejectHandler type.
     */
    private String rejectHandlerType;

    /**
     * If enhance reject.
     */
    private boolean rejectEnhanced = true;

    /**
     * Whether to wait for scheduled tasks to complete on shutdown,
     * not interrupting running tasks and executing all tasks in the queue.
     */
    protected boolean waitForTasksToCompleteOnShutdown = true;

    /**
     * The maximum number of seconds that this executor is supposed to block
     * on shutdown in order to wait for remaining tasks to complete their execution
     * before the rest of the container continues to shut down.
     */
    protected int awaitTerminationSeconds = 3;

    /**
     * Thread pool stat provider
     */
    private ThreadPoolStatProvider threadPoolStatProvider;

    private ExecutorWrapper() {
    }

    /**
     * Instantiates a new Executor wrapper.
     *
     * @param executor the DtpExecutor
     */
    public ExecutorWrapper(DtpExecutor executor) {
        this.executor = executor;
        this.threadPoolName = executor.getThreadPoolName();
        this.notifyItems = executor.getNotifyItems();
        this.platformIds = executor.getPlatformIds();
        this.taskWrappers = executor.getTaskWrappers();
        this.pluginNames = executor.getPluginNames();
        this.rejectHandlerType = executor.getRejectHandlerType();
        this.threadPoolStatProvider = ThreadPoolStatProvider.of(this);
    }

    /**
     * Instantiates a new Executor wrapper.
     *
     * @param threadPoolName the thread pool name
     * @param executor       the executor
     */
    public ExecutorWrapper(String threadPoolName, Executor executor) {
        this.threadPoolName = threadPoolName;
        if (executor instanceof ThreadPoolExecutor) {
            this.executor = new ThreadPoolExecutorAdapter((ThreadPoolExecutor) executor);
        } else if (executor instanceof ExecutorAdapter<?>) {
            this.executor = (ExecutorAdapter<?>) executor;
        } else {
            throw new IllegalArgumentException("unsupported Executor type !");
        }
        this.notifyItems = NotifyItem.getAllNotifyItems();
        AlarmManager.initAlarm(threadPoolName, notifyItems);
        this.threadPoolStatProvider = ThreadPoolStatProvider.of(this);
    }

    /**
     * Create executor wrapper.
     *
     * @param executor the executor
     * @return the executor wrapper
     */
    public static ExecutorWrapper of(DtpExecutor executor) {
        return new ExecutorWrapper(executor);
    }

    /**
     * capture executor
     *
     * @return ExecutorWrapper
     */
    public ExecutorWrapper capture() {
        ExecutorWrapper executorWrapper = new ExecutorWrapper();
        BeanUtils.copyProperties(this, executorWrapper);
        executorWrapper.executor = new CapturedExecutor(this.getExecutor());
        return executorWrapper;
    }

    /**
     * Initialize.
     */
    public void initialize() {
        if (isDtpExecutor()) {
            DtpExecutor dtpExecutor = (DtpExecutor) getExecutor();
            initialize(dtpExecutor);
            AwareManager.register(this);
        } else if (isThreadPoolExecutor()) {
            AwareManager.register(this);
        }
    }

    public void initialize(DtpExecutor dtpExecutor) {
        NotifyHelper.initNotify(dtpExecutor);
        if (preStartAllCoreThreads) {
            dtpExecutor.prestartAllCoreThreads();
        }
        // reset reject handler in initialize phase according to rejectEnhanced
        setRejectHandler(RejectHandlerGetter.buildRejectedHandler(getRejectHandlerType()));
    }

    /**
     * whether is DtpExecutor
     *
     * @return boolean
     */
    public boolean isDtpExecutor() {
        return this.executor instanceof DtpExecutor;
    }

    /**
     * whether is ThreadPoolExecutor
     *
     * @return boolean
     */
    public boolean isThreadPoolExecutor() {
        return this.executor instanceof ThreadPoolExecutorAdapter;
    }

    /**
     * set taskWrappers
     *
     * @param taskWrappers taskWrappers
     */
    public void setTaskWrappers(List<TaskWrapper> taskWrappers) {
        this.taskWrappers = taskWrappers;
        if (executor.getOriginal() instanceof TaskEnhanceAware) {
            ((TaskEnhanceAware) executor.getOriginal()).setTaskWrappers(taskWrappers);
        }
    }

    public void setRejectEnhanced(boolean rejectEnhanced) {
        this.rejectEnhanced = rejectEnhanced;
        if (isDtpExecutor()) {
            ((DtpExecutor) executor).setRejectEnhanced(rejectEnhanced);
        }
    }

    public void setRejectHandler(RejectedExecutionHandler handler) {
        this.rejectHandlerType = handler.getClass().getSimpleName();
        if (!isRejectEnhanced()) {
            executor.setRejectedExecutionHandler(handler);
            return;
        }
        executor.setRejectedExecutionHandler(RejectHandlerGetter.getProxy(handler));
    }
}
