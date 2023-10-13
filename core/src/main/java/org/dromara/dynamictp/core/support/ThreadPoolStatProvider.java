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

import lombok.val;
import org.dromara.dynamictp.common.spring.ApplicationContextHolder;
import org.dromara.dynamictp.common.timer.HashedWheelTimer;
import org.dromara.dynamictp.common.timer.Timeout;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.timer.QueueTimeoutTimerTask;
import org.dromara.dynamictp.core.timer.RunTimeoutTimerTask;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * Stat provider for thread pool.
 *
 * @author hanli
 * @since 1.1.4
 */
public class ThreadPoolStatProvider {

    private final ExecutorWrapper executorWrapper;

    /**
     * Task execute timeout, unit (ms), just for statistics.
     */
    private long runTimeout = 0;

    /**
     * Task queue wait timeout, unit (ms), just for statistics.
     */
    private long queueTimeout = 0;

    /**
     * Total reject count.
     */
    private final LongAdder rejectCount = new LongAdder();

    /**
     * Count run timeout tasks.
     */
    private final LongAdder runTimeoutCount = new LongAdder();

    /**
     * Count queue wait timeout tasks.
     */
    private final LongAdder queueTimeoutCount = new LongAdder();

    /**
     * runTimeoutMap  key -> Runnable  value -> Timeout
     */
    private final Map<Runnable, SoftReference<Timeout>> runTimeoutMap = new ConcurrentHashMap<>();

    /**
     * queueTimeoutMap  key -> Runnable  value -> Timeout
     */
    private final Map<Runnable, SoftReference<Timeout>> queueTimeoutMap = new ConcurrentHashMap<>();

    /**
     * performance monitor
     */
    private final TpPerformanceProvider tpPerformanceProvider = new TpPerformanceProvider();

    private ThreadPoolStatProvider(ExecutorWrapper executorWrapper) {
        this.executorWrapper = executorWrapper;
    }

    public static ThreadPoolStatProvider of(ExecutorWrapper executorWrapper) {
        val provider = new ThreadPoolStatProvider(executorWrapper);
        if (executorWrapper.isDtpExecutor()) {
            val dtpExecutor = (DtpExecutor) executorWrapper.getExecutor();
            provider.setRunTimeout(dtpExecutor.getRunTimeout());
            provider.setQueueTimeout(dtpExecutor.getQueueTimeout());
        }
        return provider;
    }

    public ExecutorWrapper getExecutorWrapper() {
        return executorWrapper;
    }

    public long getRunTimeout() {
        return runTimeout;
    }

    public void setRunTimeout(long runTimeout) {
        this.runTimeout = runTimeout;
    }

    public long getQueueTimeout() {
        return queueTimeout;
    }

    public void setQueueTimeout(long queueTimeout) {
        this.queueTimeout = queueTimeout;
    }

    public long getRejectedTaskCount() {
        return rejectCount.sum();
    }

    public void incRejectCount(int count) {
        rejectCount.add(count);
    }

    public long getRunTimeoutCount() {
        return runTimeoutCount.sum();
    }

    public void incRunTimeoutCount(int count) {
        runTimeoutCount.add(count);
    }

    public long getQueueTimeoutCount() {
        return queueTimeoutCount.sum();
    }

    public void incQueueTimeoutCount(int count) {
        queueTimeoutCount.add(count);
    }

    public void startQueueTimeoutTask(Runnable r) {
        if (queueTimeout <= 0) {
            return;
        }
        HashedWheelTimer timer = ApplicationContextHolder.getBean(HashedWheelTimer.class);
        QueueTimeoutTimerTask timerTask = new QueueTimeoutTimerTask(executorWrapper, r);
        queueTimeoutMap.put(r, new SoftReference<>(timer.newTimeout(timerTask, queueTimeout, TimeUnit.MILLISECONDS)));
    }

    public void cancelQueueTimeoutTask(Runnable r) {
        Optional.ofNullable(queueTimeoutMap.remove(r))
                .map(SoftReference::get)
                .ifPresent(Timeout::cancel);
    }

    public void startRunTimeoutTask(Thread t, Runnable r) {
        if (runTimeout <= 0) {
            return;
        }
        HashedWheelTimer timer = ApplicationContextHolder.getBean(HashedWheelTimer.class);
        RunTimeoutTimerTask timerTask = new RunTimeoutTimerTask(executorWrapper, r, t);
        runTimeoutMap.put(r, new SoftReference<>(timer.newTimeout(timerTask, runTimeout, TimeUnit.MILLISECONDS)));
    }

    public void cancelRunTimeoutTask(Runnable r) {
        Optional.ofNullable(runTimeoutMap.remove(r))
                .map(SoftReference::get)
                .ifPresent(Timeout::cancel);
    }

    public TpPerformanceProvider getPerformanceProvider() {
        return this.tpPerformanceProvider;
    }

}
