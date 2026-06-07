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

package org.dromara.dynamictp.test.core.support.proxy;

import org.dromara.dynamictp.core.support.proxy.ScheduledThreadPoolExecutorProxy;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ScheduledThreadPoolExecutorProxy test.
 *
 * @author codex
 */
class ScheduledThreadPoolExecutorProxyTest {

    private ScheduledThreadPoolExecutorProxy proxy;

    @AfterEach
    void tearDown() {
        if (proxy != null) {
            proxy.shutdownNow();
        }
    }

    @Test
    void testScheduleMethodsApplyTaskWrappers() throws Exception {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        proxy = new ScheduledThreadPoolExecutorProxy(executor);
        AtomicInteger wrappedRuns = new AtomicInteger();
        TaskWrapper wrapper = runnable -> () -> {
            wrappedRuns.incrementAndGet();
            runnable.run();
        };
        proxy.setTaskWrappers(Collections.singletonList(wrapper));

        proxy.execute(() -> { });
        proxy.schedule(() -> { }, 0, TimeUnit.MILLISECONDS).get(2, TimeUnit.SECONDS);
        assertEquals("result",
                proxy.schedule(() -> { }, "result", 0, TimeUnit.MILLISECONDS).get(2, TimeUnit.SECONDS));

        CountDownLatch fixedRateLatch = new CountDownLatch(1);
        ScheduledFuture<?> fixedRate = proxy.scheduleAtFixedRate(
                fixedRateLatch::countDown, 0, 1, TimeUnit.DAYS);
        assertTrue(fixedRateLatch.await(2, TimeUnit.SECONDS));
        fixedRate.cancel(true);

        CountDownLatch fixedDelayLatch = new CountDownLatch(1);
        ScheduledFuture<?> fixedDelay = proxy.scheduleWithFixedDelay(
                fixedDelayLatch::countDown, 0, 1, TimeUnit.DAYS);
        assertTrue(fixedDelayLatch.await(2, TimeUnit.SECONDS));
        fixedDelay.cancel(true);

        proxy.shutdown();
        assertTrue(proxy.awaitTermination(2, TimeUnit.SECONDS));
        assertEquals(6, wrappedRuns.get());
    }

    @Test
    void testProxyPropertiesCanBeUpdated() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
        proxy = new ScheduledThreadPoolExecutorProxy(executor);
        TaskWrapper wrapper = runnable -> runnable;

        proxy.setRejectHandlerType("custom");
        proxy.setTaskWrappers(Collections.singletonList(wrapper));

        assertEquals("custom", proxy.getRejectHandlerType());
        assertSame(wrapper, proxy.getTaskWrappers().get(0));
        assertSame(executor.getThreadFactory(), proxy.getThreadFactory());
        assertEquals(executor.getCorePoolSize(), proxy.getCorePoolSize());
    }
}
