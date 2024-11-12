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

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

/**
 * DtpLifecycleSupport which mainly implements ThreadPoolExecutor's lifecycle management.
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
        if (executorWrapper.isExecutorService()) {
            ExecutorService executorService = (ExecutorService) executorWrapper.getExecutor().getOriginal();
            internalShutdown(executorService,
                    executorWrapper.getThreadPoolName(),
                    executorWrapper.isWaitForTasksToCompleteOnShutdown(),
                    executorWrapper.getAwaitTerminationSeconds());
        }
    }

    public static void shutdownGracefulAsync(ExecutorService executor,
                                             String threadPoolName,
                                             int timeout) {
        ExecutorService tmpExecutor = Executors.newSingleThreadExecutor();
        tmpExecutor.execute(() -> internalShutdown(executor, threadPoolName,
                true, timeout));
        tmpExecutor.shutdown();
    }

    /**
     * Perform a shutdown on the underlying ExecutorService.
     * @param executor the executor to shut down (maybe {@code null})
     * @param threadPoolName the name of the thread pool (for logging purposes)
     * @param waitForTasksToCompleteOnShutdown whether to wait for tasks to complete on shutdown
     * @param awaitTerminationSeconds the maximum number of seconds to wait
     *
     * @see ExecutorService#shutdown()
     * @see ExecutorService#shutdownNow()
     */
    public static void internalShutdown(ExecutorService executor,
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
    private static void awaitTerminationIfNecessary(ExecutorService executor,
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
