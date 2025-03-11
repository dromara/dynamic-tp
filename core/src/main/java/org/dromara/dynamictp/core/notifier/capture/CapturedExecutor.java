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

package org.dromara.dynamictp.core.notifier.capture;

import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.notifier.AbstractDtpNotifier;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.support.adapter.ExecutorAdapter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;

/**
 * CapturedExecutor implements ExecutorAdapter, the goal of this class
 * is to capture DtpExecutor's status when construct {@link BaseNotifyCtx} during {@link AlarmManager#doTryAlarm}.
 * <p>
 * In other words, this can ensure that the thread pool status when the alarm threshold is triggered is
 * consistent with the content in the {@link AbstractDtpNotifier#buildAlarmContent(NotifyPlatform, NotifyItemEnum)}
 *
 * @author ruoan
 * @since 1.1.3
 */
public final class CapturedExecutor implements ExecutorAdapter<ExecutorAdapter<?>> {

    private final ExecutorAdapter<?> originExecutor;

    private final int corePoolSize;

    private final int maximumPoolSize;

    private final int activeCount;

    private final int poolSize;

    private final int largestPoolSize;

    private final long taskCount;

    private final long completedTaskCount;

    private final long keepAliveTime;

    private final boolean allowCoreThreadTimeOut;

    private final RejectedExecutionHandler rejectedExecutionHandler;

    /**
     * @see DtpExecutor#getRejectHandlerType
     */
    private final String rejectHandlerType;

    /**
     * @see CapturedBlockingQueue
     */
    private final CapturedBlockingQueue blockingQueue;

    public CapturedExecutor(ExecutorAdapter<?> executorAdapter) {
        this.originExecutor = executorAdapter;
        this.corePoolSize = executorAdapter.getCorePoolSize();
        this.maximumPoolSize = executorAdapter.getMaximumPoolSize();
        this.activeCount = executorAdapter.getActiveCount();
        this.poolSize = executorAdapter.getPoolSize();
        this.largestPoolSize = executorAdapter.getLargestPoolSize();
        this.taskCount = executorAdapter.getTaskCount();
        this.completedTaskCount = executorAdapter.getCompletedTaskCount();
        this.keepAliveTime = executorAdapter.getKeepAliveTime(TimeUnit.SECONDS);
        this.allowCoreThreadTimeOut = executorAdapter.allowsCoreThreadTimeOut();
        this.rejectedExecutionHandler = executorAdapter.getRejectedExecutionHandler();
        this.rejectHandlerType = executorAdapter.getRejectHandlerType();
        this.blockingQueue = new CapturedBlockingQueue(executorAdapter);
    }

    @Override
    public ExecutorAdapter<?> getOriginal() {
        return originExecutor;
    }

    @Override
    public void execute(Runnable command) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCorePoolSize() {
        return corePoolSize;
    }

    @Override
    public void setCorePoolSize(int corePoolSize) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    @Override
    public void setMaximumPoolSize(int maximumPoolSize) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPoolSize() {
        return poolSize;
    }

    @Override
    public int getActiveCount() {
        return activeCount;
    }

    @Override
    public int getLargestPoolSize() {
        return largestPoolSize;
    }

    @Override
    public long getTaskCount() {
        return taskCount;
    }

    @Override
    public long getCompletedTaskCount() {
        return completedTaskCount;
    }

    @Override
    public BlockingQueue<Runnable> getQueue() {
        return blockingQueue;
    }

    @Override
    public String getQueueType() {
        return blockingQueue.getQueueType();
    }

    @Override
    public int getQueueSize() {
        return blockingQueue.size();
    }

    @Override
    public int getQueueRemainingCapacity() {
        return blockingQueue.remainingCapacity();
    }

    @Override
    public int getQueueCapacity() {
        return blockingQueue.getQueueCapacity();
    }

    @Override
    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return rejectedExecutionHandler;
    }

    @Override
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRejectHandlerType() {
        return rejectHandlerType;
    }

    @Override
    public boolean allowsCoreThreadTimeOut() {
        return allowCoreThreadTimeOut;
    }

    @Override
    public void allowCoreThreadTimeOut(boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getKeepAliveTime(TimeUnit unit) {
        return unit.convert(keepAliveTime, TimeUnit.SECONDS);
    }

    @Override
    public void setKeepAliveTime(long time, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }
}
