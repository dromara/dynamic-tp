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

package org.dromara.dynamictp.core.thread;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.core.notifier.alarm.ThreadPoolAlarm;
import org.dromara.dynamictp.core.notifier.alarm.ThreadPoolAlarmHelper;
import org.dromara.dynamictp.core.notifier.manager.NotifyHelper;
import org.dromara.dynamictp.core.reject.RejectHandlerGetter;
import org.dromara.dynamictp.core.spring.SpringExecutor;
import org.dromara.dynamictp.core.support.ExecutorAdapter;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.task.runnable.DtpRunnable;
import org.dromara.dynamictp.core.support.task.runnable.NamedRunnable;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;
import org.slf4j.MDC;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.TRACE_ID;

/**
 * Dynamic ThreadPoolExecutor, extending ThreadPoolExecutor, implements some new features
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpExecutor extends ThreadPoolExecutor
        implements SpringExecutor, ExecutorAdapter<ThreadPoolExecutor>, ThreadPoolAlarm {

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
     * Notify platform ids.
     */
    private List<String> platformIds;

    /**
     * Task wrappers, do sth enhanced.
     */
    private List<TaskWrapper> taskWrappers = Lists.newArrayList();

    /**
     * Plugin names.
     */
    private Set<String> pluginNames = Sets.newHashSet();

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
     * alarm helper
     */
    private final ThreadPoolAlarmHelper alarmHelper;

    /**
     * Whether to wait for scheduled tasks to complete on shutdown,
     * not interrupting running tasks and executing all tasks in the queue.
     */
    protected boolean waitForTasksToCompleteOnShutdown = false;

    /**
     * The maximum number of seconds that this executor is supposed to block
     * on shutdown in order to wait for remaining tasks to complete their execution
     * before the rest of the container continues to shut down.
     */
    protected int awaitTerminationSeconds = 0;

    public DtpExecutor(int corePoolSize,
                       int maximumPoolSize,
                       long keepAliveTime,
                       TimeUnit unit,
                       BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                Executors.defaultThreadFactory(), new AbortPolicy());
    }

    public DtpExecutor(int corePoolSize,
                       int maximumPoolSize,
                       long keepAliveTime,
                       TimeUnit unit,
                       BlockingQueue<Runnable> workQueue,
                       ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                threadFactory, new AbortPolicy());
    }

    public DtpExecutor(int corePoolSize,
                       int maximumPoolSize,
                       long keepAliveTime,
                       TimeUnit unit,
                       BlockingQueue<Runnable> workQueue,
                       RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                Executors.defaultThreadFactory(), handler);
    }

    public DtpExecutor(int corePoolSize,
                       int maximumPoolSize,
                       long keepAliveTime,
                       TimeUnit unit,
                       BlockingQueue<Runnable> workQueue,
                       ThreadFactory threadFactory,
                       RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        alarmHelper = ThreadPoolAlarmHelper.of(ExecutorWrapper.of(this));
    }

    @Override
    public ThreadPoolExecutor getOriginal() {
        return this;
    }
    
    @Override
    public void execute(Runnable task, long startTimeout) {
        execute(task);
    }

    @Override
    public void execute(Runnable command) {
        // 在这里计算动态线程池任务维度的QPS
        DtpRunnable dtpRunnable = (DtpRunnable) wrapTasks(command);
        executeAlarmEnhance(dtpRunnable);
        super.execute(dtpRunnable);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        beforeExecuteAlarmEnhance(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        afterExecuteAlarmEnhance(r, t);
        tryPrintError(r, t);
        clearContext();
    }

    public void initialize() {
        NotifyHelper.initNotify(this);
        if (preStartAllCoreThreads) {
            prestartAllCoreThreads();
        }
        // reset reject handler in initialize phase according to rejectEnhanced
        setRejectHandler(RejectHandlerGetter.buildRejectedHandler(getRejectHandlerType()));
    }

    protected Runnable wrapTasks(Runnable command) {
        if (CollectionUtils.isNotEmpty(taskWrappers)) {
            for (TaskWrapper t : taskWrappers) {
                command = t.wrap(command);
            }
        }
        String taskName = (command instanceof NamedRunnable) ? ((NamedRunnable) command).getName() : null;
        command = new DtpRunnable(command, taskName);
        return command;
    }

    private void clearContext() {
        MDC.remove(TRACE_ID);
    }

    private void tryPrintError(Runnable r, Throwable t) {
        if (Objects.nonNull(t)) {
            log.error("thread {} throw exception {}", Thread.currentThread(), t.getMessage(), t);
            return;
        }
        if (r instanceof FutureTask) {
            try {
                Future<?> future = (Future<?>) r;
                future.get();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("thread {} throw exception {}", Thread.currentThread(), e.getMessage(), e);
            }
        }
    }

    public void setRejectHandler(RejectedExecutionHandler handler) {
        this.rejectHandlerType = handler.getClass().getSimpleName();
        if (!isRejectEnhanced()) {
            setRejectedExecutionHandler(handler);
            return;
        }
        setRejectedExecutionHandler(RejectHandlerGetter.getProxy(handler));
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

    public List<TaskWrapper> getTaskWrappers() {
        return taskWrappers;
    }

    public void setTaskWrappers(List<TaskWrapper> taskWrappers) {
        this.taskWrappers = taskWrappers;
    }

    public Set<String> getPluginNames() {
        return pluginNames;
    }

    public void setPluginNames(Set<String> pluginNames) {
        this.pluginNames = pluginNames;
    }

    public boolean isPreStartAllCoreThreads() {
        return preStartAllCoreThreads;
    }

    public void setPreStartAllCoreThreads(boolean preStartAllCoreThreads) {
        this.preStartAllCoreThreads = preStartAllCoreThreads;
    }

    public boolean isRejectEnhanced() {
        return rejectEnhanced;
    }

    public void setRejectEnhanced(boolean rejectEnhanced) {
        this.rejectEnhanced = rejectEnhanced;
    }

    @Override
    public String getRejectHandlerType() {
        return rejectHandlerType;
    }

    public void setRejectHandlerType(String rejectHandlerType) {
        this.rejectHandlerType = rejectHandlerType;
    }

    @Override
    public long getRejectedTaskCount() {
        return alarmHelper.getRejectedTaskCount();
    }

    public long getRunTimeout() {
        return alarmHelper.getRunTimeout();
    }

    public void setRunTimeout(long runTimeout) {
        alarmHelper.setRunTimeout(runTimeout);
    }

    public long getQueueTimeout() {
        return alarmHelper.getQueueTimeout();
    }

    public void setQueueTimeout(long queueTimeout) {
        alarmHelper.setQueueTimeout(queueTimeout);
    }


    public long getRunTimeoutCount() {
        return alarmHelper.getRunTimeoutCount();
    }

    public long getQueueTimeoutCount() {
        return alarmHelper.getQueueTimeoutCount();
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

    /**
     * In order for the field can be assigned by reflection.
     *
     * @param allowCoreThreadTimeOut allowCoreThreadTimeOut
     */
    public void setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        allowCoreThreadTimeOut(allowCoreThreadTimeOut);
    }

    @Override
    public ThreadPoolAlarmHelper getThirdPartTpAlarmHelper() {
        return alarmHelper;
    }
}
