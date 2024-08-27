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

import lombok.Data;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.util.BeanCopierUtils;
import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.aware.TaskEnhanceAware;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.notifier.capture.CapturedExecutor;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
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
     * Thread pool name.
     */
    private String threadPoolName;

    /**
     * Thread pool alias name.
     */
    private String threadPoolAliasName;

    /**
     * Executor.
     */
    private ExecutorAdapter<?> executor;

    /**
     * Notify items, see {@link NotifyItemEnum}.
     */
    private List<NotifyItem> notifyItems;

    /**
     * Notify platform ids.
     */
    private List<String> platformIds;

    /**
     * Whether to enable notification.
     */
    private boolean notifyEnabled = true;

    /**
     * Thread pool stat provider
     */
    private ThreadPoolStatProvider threadPoolStatProvider;

    /**
     * Aware names
     */
    private Set<String> awareNames = new HashSet<>();

    private ExecutorWrapper() {
    }

    /**
     * Instantiates a new Executor wrapper.
     *
     * @param executor the DtpExecutor
     */
    public ExecutorWrapper(DtpExecutor executor) {
        this.threadPoolName = executor.getThreadPoolName();
        this.threadPoolAliasName = executor.getThreadPoolAliasName();
        this.executor = executor;
        this.notifyItems = executor.getNotifyItems();
        this.notifyEnabled = executor.isNotifyEnabled();
        this.platformIds = executor.getPlatformIds();
        this.awareNames = executor.getAwareNames();
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
        BeanCopierUtils.copyProperties(this, executorWrapper);
        executorWrapper.executor = new CapturedExecutor(this.getExecutor());
        return executorWrapper;
    }
    /**
     * Initialize.
     */
    public void initialize() {
        if (isDtpExecutor()) {
            DtpExecutor dtpExecutor = (DtpExecutor) getExecutor();
            dtpExecutor.initialize();
            AwareManager.register(this);
        } else if (isThreadPoolExecutor()) {
            AwareManager.register(this);
        }
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
        if (executor.getOriginal() instanceof TaskEnhanceAware) {
            ((TaskEnhanceAware) executor.getOriginal()).setTaskWrappers(taskWrappers);
        }
    }
}
