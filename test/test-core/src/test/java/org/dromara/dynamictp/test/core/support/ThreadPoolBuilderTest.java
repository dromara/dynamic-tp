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

import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.support.ThreadPoolBuilder;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
public class ThreadPoolBuilderTest {

    @Test
    void testBuildDynamicThrowsWhenBothPriorityAndOrderedSet() {
        assertThrows(IllegalArgumentException.class, () -> ThreadPoolBuilder.newBuilder()
                .threadPoolName("dtpExecutor1")
                .priority()
                .ordered()
                .buildDynamic());
    }

    @Test
    void testBuildDynamic() {
        DtpExecutor executor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("testDynamic")
                .corePoolSize(4)
                .maximumPoolSize(8)
                .keepAliveTime(30)
                .timeUnit(TimeUnit.SECONDS)
                .buildDynamic();

        assertNotNull(executor);
        assertEquals("testDynamic", executor.getThreadPoolName());
        assertEquals(4, executor.getCorePoolSize());
        assertEquals(8, executor.getMaximumPoolSize());
        executor.shutdown();
    }

    @Test
    void testBuildCommon() {
        ThreadPoolExecutor executor = ThreadPoolBuilder.newBuilder()
                .corePoolSize(2)
                .maximumPoolSize(4)
                .keepAliveTime(60)
                .timeUnit(TimeUnit.SECONDS)
                .buildCommon();

        assertNotNull(executor);
        assertInstanceOf(ThreadPoolExecutor.class, executor);
        assertEquals(2, executor.getCorePoolSize());
        assertEquals(4, executor.getMaximumPoolSize());
        executor.shutdown();
    }

    @Test
    void testBuildCommonKeepsDefaultsForInvalidOptions() {
        ThreadPoolExecutor executor = ThreadPoolBuilder.newBuilder()
                .corePoolSize(-1)
                .maximumPoolSize(0)
                .keepAliveTime(0)
                .timeUnit(null)
                .buildCommon();

        assertEquals(1, executor.getCorePoolSize());
        assertEquals(Runtime.getRuntime().availableProcessors(), executor.getMaximumPoolSize());
        assertEquals(60, executor.getKeepAliveTime(TimeUnit.SECONDS));
        executor.shutdown();
    }

    @Test
    void testBuildDynamicAppliesOptionalSettings() {
        DtpExecutor executor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("dynamic-options")
                .corePoolSize(1)
                .maximumPoolSize(2)
                .allowCoreThreadTimeOut(true)
                .waitForTasksToCompleteOnShutdown(false)
                .awaitTerminationSeconds(7)
                .preStartAllCoreThreads(true)
                .rejectEnhanced(false)
                .notifyEnabled(false)
                .runTimeout(100)
                .tryInterrupt(true)
                .queueTimeout(200)
                .platformIds(Collections.singletonList("platform-1"))
                .buildDynamic();

        assertTrue(executor.allowsCoreThreadTimeOut());
        assertFalse(executor.isWaitForTasksToCompleteOnShutdown());
        assertEquals(7, executor.getAwaitTerminationSeconds());
        assertTrue(executor.isPreStartAllCoreThreads());
        assertFalse(executor.isRejectEnhanced());
        assertFalse(executor.isNotifyEnabled());
        assertEquals(100, executor.getRunTimeout());
        assertTrue(executor.isTryInterrupt());
        assertEquals(200, executor.getQueueTimeout());
        assertEquals(Collections.singletonList("platform-1"), executor.getPlatformIds());
        executor.shutdown();
    }

    @Test
    void testBuildScheduledCommon() {
        ScheduledExecutorService executor = ThreadPoolBuilder.newBuilder()
                .dynamic(false)
                .corePoolSize(2)
                .buildScheduled();

        assertInstanceOf(ScheduledThreadPoolExecutor.class, executor);
        assertEquals(2, ((ScheduledThreadPoolExecutor) executor).getCorePoolSize());
        executor.shutdown();
    }

    @Test
    void testBuildWithTtlCommonExecutesTask() throws InterruptedException {
        ExecutorService executor = ThreadPoolBuilder.newBuilder()
                .dynamic(false)
                .buildWithTtl();
        CountDownLatch latch = new CountDownLatch(1);
        try {
            executor.execute(latch::countDown);

            assertTrue(latch.await(3, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void testBuildPriority() {
        assertNotNull(ThreadPoolBuilder.newBuilder()
                .threadPoolName("priorityPool")
                .corePoolSize(2)
                .maximumPoolSize(4)
                .buildPriority());
    }

}
