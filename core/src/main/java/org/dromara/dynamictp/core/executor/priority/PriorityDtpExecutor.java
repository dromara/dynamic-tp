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

package org.dromara.dynamictp.core.executor.priority;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.support.task.runnable.DtpRunnable;

import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * PriorityDtpExecutor related, extending DtpExecutor, implements priority feature
 *
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 * @since 1.1.7
 */
@Slf4j
public class PriorityDtpExecutor extends DtpExecutor {

    public PriorityDtpExecutor(int corePoolSize,
                               int maximumPoolSize,
                               long keepAliveTime,
                               TimeUnit unit,
                               int capacity) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, new PriorityBlockingQueue<>(capacity), Executors.defaultThreadFactory(), new AbortPolicy());
    }

    public PriorityDtpExecutor(int corePoolSize,
                               int maximumPoolSize,
                               long keepAliveTime,
                               TimeUnit unit,
                               int capacity,
                               ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, new PriorityBlockingQueue<>(capacity), threadFactory, new AbortPolicy());
    }

    public PriorityDtpExecutor(int corePoolSize,
                               int maximumPoolSize,
                               long keepAliveTime,
                               TimeUnit unit,
                               int capacity,
                               RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, new PriorityBlockingQueue<>(capacity), Executors.defaultThreadFactory(), handler);
    }

    public PriorityDtpExecutor(int corePoolSize,
                               int maximumPoolSize,
                               long keepAliveTime,
                               TimeUnit unit,
                               int capacity,
                               ThreadFactory threadFactory,
                               RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, new PriorityBlockingQueue<>(capacity), threadFactory, handler);
    }


    public PriorityDtpExecutor(int corePoolSize,
                               int maximumPoolSize,
                               long keepAliveTime,
                               TimeUnit unit,
                               PriorityBlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), new AbortPolicy());
    }

    public PriorityDtpExecutor(int corePoolSize,
                               int maximumPoolSize,
                               long keepAliveTime,
                               TimeUnit unit,
                               PriorityBlockingQueue<Runnable> workQueue,
                               ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, new AbortPolicy());
    }

    public PriorityDtpExecutor(int corePoolSize,
                               int maximumPoolSize,
                               long keepAliveTime,
                               TimeUnit unit,
                               PriorityBlockingQueue<Runnable> workQueue,
                               RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), handler);
    }

    public PriorityDtpExecutor(int corePoolSize,
                               int maximumPoolSize,
                               long keepAliveTime,
                               TimeUnit unit,
                               PriorityBlockingQueue<Runnable> workQueue,
                               ThreadFactory threadFactory,
                               RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new PriorityFutureTask<>(runnable, value);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new PriorityFutureTask<>(callable);
    }

    public void execute(Runnable command, int priority) {
        super.execute(PriorityRunnable.of(command, priority));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(PriorityRunnable.of(task, Priority.LOWEST_PRECEDENCE));
    }

    public Future<?> submit(Runnable task, int priority) {
        return super.submit(PriorityRunnable.of(task, priority));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return super.submit(PriorityRunnable.of(task, Priority.LOWEST_PRECEDENCE), result);
    }

    public <T> Future<T> submit(Runnable task, T result, int priority) {
        return super.submit(PriorityRunnable.of(task, priority), result);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(PriorityCallable.of(task, Priority.LOWEST_PRECEDENCE));
    }

    public <T> Future<T> submit(Callable<T> task, int priority) {
        return super.submit(PriorityCallable.of(task, priority));
    }

    /**
     * Priority Comparator
     *
     * @return Comparator
     */
    public static Comparator<Runnable> getRunnableComparator() {
        return (o1, o2) -> {
            if (!(o1 instanceof DtpRunnable) || !(o2 instanceof DtpRunnable)) {
                return 0;
            }
            Runnable po1 = ((DtpRunnable) o1).getOriginRunnable();
            Runnable po2 = ((DtpRunnable) o2).getOriginRunnable();
            if (po1 instanceof Priority && po2 instanceof Priority) {
                return Integer.compare(((Priority) po1).getPriority(), ((Priority) po2).getPriority());
            }
            return 0;
        };
    }

}
