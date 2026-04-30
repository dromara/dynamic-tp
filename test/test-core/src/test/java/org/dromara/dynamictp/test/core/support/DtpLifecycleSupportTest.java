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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void testCancelRemainingTaskWithFuture() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
        });

        // shutdownNow returns remaining tasks
        executor.shutdownNow();
        // The future should be cancellable
        future.cancel(true);
        assertTrue(future.isCancelled());

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

        // Wait for async shutdown to complete
        Thread.sleep(1000);
        assertTrue(executor.isShutdown());
    }

    @Test
    void testShutdownAlreadyTerminated() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> { });
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }

        assertTrue(executor.isTerminated());
        // Should not throw when called on already terminated executor
        assertDoesNotThrow(() -> DtpLifecycleSupport.internalShutdown(executor, "terminated", true, 1));
    }
}
