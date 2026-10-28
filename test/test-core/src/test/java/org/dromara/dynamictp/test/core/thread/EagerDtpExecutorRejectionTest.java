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

package org.dromara.dynamictp.test.core.thread;

import org.dromara.dynamictp.core.executor.eager.EagerDtpExecutor;
import org.dromara.dynamictp.core.executor.eager.TaskQueue;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EagerDtpExecutorRejectionTest {

    @Test
    void forcedEnqueuePreservesTaskWrapper() throws Exception {
        CyclicBarrier offered = new CyclicBarrier(2);
        AtomicInteger failedOffers = new AtomicInteger();
        TaskQueue queue = new TaskQueue(1) {
            @Override
            public boolean offer(Runnable task) {
                boolean accepted = super.offer(task);
                if (!accepted && failedOffers.incrementAndGet() <= 2) {
                    // 两个提交线程均尝试扩容，确保其中一个进入补偿入队。
                    try {
                        offered.await(10, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
                return accepted;
            }
        };
        EagerDtpExecutor executor = new EagerDtpExecutor(1, 2, 1, TimeUnit.SECONDS, queue);
        queue.setExecutor(executor);
        ExecutorService producers = Executors.newFixedThreadPool(2);
        AtomicInteger wrapped = new AtomicInteger();
        AtomicInteger executed = new AtomicInteger();
        executor.setTaskWrappers(Collections.singletonList(task -> {
            wrapped.incrementAndGet();
            return () -> {
                executed.incrementAndGet();
                task.run();
            };
        }));
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try {
            executor.execute(() -> {
                started.countDown();
                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            assertTrue(started.await(10, TimeUnit.SECONDS));
            Future<CompletableFuture<Integer>> first = producers.submit(
                    () -> CompletableFuture.supplyAsync(() -> awaitAndReturn(release, 42), executor));
            Future<CompletableFuture<Integer>> second = producers.submit(
                    () -> CompletableFuture.supplyAsync(() -> awaitAndReturn(release, 43), executor));
            CompletableFuture<Integer> firstResult = first.get(10, TimeUnit.SECONDS);
            CompletableFuture<Integer> secondResult = second.get(10, TimeUnit.SECONDS);
            assertEquals(1, queue.size());
            assertThrows(RejectedExecutionException.class, () -> executor.execute(() -> { }));
            assertEquals(3, executor.getSubmittedTaskCount());
            release.countDown();
            assertEquals(42, firstResult.get(10, TimeUnit.SECONDS).intValue());
            assertEquals(43, secondResult.get(10, TimeUnit.SECONDS).intValue());
            executor.shutdown();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
            assertEquals(4, wrapped.get());
            assertEquals(3, executed.get());
            assertEquals(0, executor.getSubmittedTaskCount());
        } finally {
            release.countDown();
            producers.shutdownNow();
            executor.shutdownNow();
        }
    }

    private int awaitAndReturn(CountDownLatch release, int value) {
        try {
            release.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
        return value;
    }

    @Test
    void rejectionAfterShutdownDoesNotLeakSubmittedCount() {
        TaskQueue queue = new TaskQueue(1);
        EagerDtpExecutor executor = new EagerDtpExecutor(1, 1, 1, TimeUnit.SECONDS, queue);
        queue.setExecutor(executor);
        executor.shutdown();

        assertThrows(RejectedExecutionException.class, () -> executor.execute(() -> { }));
        assertEquals(0, executor.getSubmittedTaskCount());
    }

    @Test
    void wrapperFailureDoesNotLeakSubmittedCount() {
        TaskQueue queue = new TaskQueue(1);
        EagerDtpExecutor executor = new EagerDtpExecutor(1, 1, 1, TimeUnit.SECONDS, queue);
        queue.setExecutor(executor);
        executor.setTaskWrappers(Collections.singletonList(task -> {
            throw new IllegalStateException("wrapper failed");
        }));
        try {
            assertThrows(IllegalStateException.class, () -> executor.execute(() -> { }));
            assertEquals(0, executor.getSubmittedTaskCount());
        } finally {
            executor.shutdownNow();
        }
    }
}
