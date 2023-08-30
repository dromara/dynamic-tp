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

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * DtpLifecycleSupport which mainly implements Spring bean's lifecycle management,
 * mimics spring internal thread pool {@link ThreadPoolTaskExecutor}.
 *
 * @author yanhom
 * @since 1.0.3
 **/
@Slf4j
public class DtpLifecycleSupport {

    private DtpLifecycleSupport() { }

    /**
     * Initialize, do sth.
     *
     * @param executorWrapper executor wrapper
     */
    public static void initialize(ExecutorWrapper executorWrapper) {
        executorWrapper.initialize();
    }

    /**
     * Calls {@code internalShutdown} when the BeanFactory destroys
     * the task executor instance.
     * @param executorWrapper executor wrapper
     */
    public static void destroy(ExecutorWrapper executorWrapper) {
        if (executorWrapper.isDtpExecutor()) {
            destroy((DtpExecutor) executorWrapper.getExecutor());
        } else if (executorWrapper.isThreadPoolExecutor()) {
            internalShutdown(((ThreadPoolExecutorAdapter) executorWrapper.getExecutor()).getOriginal(),
                    executorWrapper.getThreadPoolName(),
                    true,
                    0);
        }
    }

    public static void destroy(DtpExecutor executor) {
        internalShutdown(executor,
                executor.getThreadPoolName(),
                executor.isWaitForTasksToCompleteOnShutdown(),
                executor.getAwaitTerminationSeconds());
    }

    /**
     * Perform a shutdown on the underlying ExecutorService.
     * @see ExecutorService#shutdown()
     * @see ExecutorService#shutdownNow()
     */
    public static void internalShutdown(ThreadPoolExecutor executor,
                                        String threadPoolName,
                                        boolean waitForTasksToCompleteOnShutdown,
                                        int awaitTerminationSeconds) {
        if (Objects.isNull(executor)) {
            return;
        }
        log.info("Shutting down ExecutorService, threadPoolName: {}", threadPoolName);
        if (waitForTasksToCompleteOnShutdown) {
            executor.shutdown();
        } else {
            for (Runnable remainingTask : executor.shutdownNow()) {
                cancelRemainingTask(remainingTask);
            }
        }
        awaitTerminationIfNecessary(executor, threadPoolName, awaitTerminationSeconds);
    }

    /**
     * Cancel the given remaining task which never commended execution,
     * as returned from {@link ExecutorService#shutdownNow()}.
     * @param task the task to cancel (typically a {@link RunnableFuture})
     * @see RunnableFuture#cancel(boolean)
     */
    protected static void cancelRemainingTask(Runnable task) {
        if (task instanceof Future) {
            ((Future<?>) task).cancel(true);
        }
    }

    /**
     * Wait for the executor to terminate, according to the value of the awaitTerminationSeconds property.
     * @param executor executor
     */
    private static void awaitTerminationIfNecessary(ThreadPoolExecutor executor,
                                                    String threadPoolName,
                                                    int awaitTerminationSeconds) {
        if (awaitTerminationSeconds <= 0) {
            return;
        }
        try {
            if (!executor.awaitTermination(awaitTerminationSeconds, TimeUnit.SECONDS)) {
                log.warn("Timed out while waiting for executor {} to terminate", threadPoolName);
            }
        } catch (InterruptedException ex) {
            log.warn("Interrupted while waiting for executor {} to terminate", threadPoolName);
            Thread.currentThread().interrupt();
        }
    }
}
