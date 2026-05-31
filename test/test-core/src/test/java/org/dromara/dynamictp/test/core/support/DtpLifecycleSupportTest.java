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

package org.dromara.dynamictp.test.core.support;

import org.dromara.dynamictp.core.support.DtpLifecycleSupport;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DtpLifecycleSupport test
 *
 * @author yanhom
 * @since 1.2.2
 */
class DtpLifecycleSupportTest {

    @Test
    void testInternalShutdownGraceful() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        });

        DtpLifecycleSupport.internalShutdown(executor, "test-graceful", true, 5);

        assertTrue(executor.isShutdown());
        assertTrue(executor.isTerminated());
    }

    @Test
    void testInternalShutdownImmediate() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
        });

        DtpLifecycleSupport.internalShutdown(executor, "test-immediate", false, 1);

        assertTrue(executor.isShutdown());
        assertTrue(executor.isTerminated());
    }

    @Test
    void testInternalShutdownNullExecutor() {
        assertDoesNotThrow(() -> DtpLifecycleSupport.internalShutdown(null, "null-pool", true, 3));
    }

    @Test
    void testInternalShutdownZeroAwait() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        DtpLifecycleSupport.internalShutdown(executor, "zero-await", true, 0);

        assertTrue(executor.isShutdown());
    }

    @Test
    void testShutdownGracefulAsync() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        });

        DtpLifecycleSupport.shutdownGracefulAsync(executor, "test-async", 5);

        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        assertTrue(executor.isShutdown());
    }

    @Test
    void testInternalShutdownCancelsFutureTasksOnShutdownNow() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // Fill the single thread so queued tasks stay in queue
        executor.submit(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
        });
        // This future will sit in queue and be cancelled by shutdownNow
        java.util.concurrent.RunnableFuture<?> future =
                new java.util.concurrent.FutureTask<>(() -> { }, null);
        executor.execute(future);

        // Immediate shutdown (shutdownNow) should cancel remaining tasks including the future
        DtpLifecycleSupport.internalShutdown(executor, "cancel-future", false, 3);

        assertTrue(executor.isShutdown());
        assertTrue(future.isCancelled());
    }
}
