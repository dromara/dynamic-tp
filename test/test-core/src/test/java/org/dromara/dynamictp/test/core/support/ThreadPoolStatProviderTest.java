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
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolStatProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ThreadPoolStatProvider test
 *
 * @author yanhom
 * @since 1.2.2
 */
class ThreadPoolStatProviderTest {

    private DtpExecutor dtpExecutor;

    @AfterEach
    void tearDown() {
        if (dtpExecutor != null) {
            dtpExecutor.shutdownNow();
        }
    }

    @Test
    void testNullRunnableOperationsDoNotThrow() {
        ThreadPoolStatProvider provider = createProvider();

        assertDoesNotThrow(() -> provider.startTask(null));
        assertDoesNotThrow(() -> provider.completeTask(null));
        assertDoesNotThrow(() -> provider.cancelRunTimeoutTask(null));
        assertDoesNotThrow(() -> provider.cancelQueueTimeoutTask(null));
    }

    @Test
    void testStartAndCompleteTaskWorkNormally() {
        ThreadPoolStatProvider provider = createProvider();
        Runnable task = () -> { };

        assertDoesNotThrow(() -> {
            provider.startTask(task);
            provider.completeTask(task);
        });
    }

    @Test
    void testRunTimeoutDefaultIsZero() {
        ThreadPoolStatProvider provider = createProvider();
        assertEquals(0L, provider.getRunTimeout());
    }

    @Test
    void testSetAndGetRunTimeout() {
        ThreadPoolStatProvider provider = createProvider();
        provider.setRunTimeout(500L);
        assertEquals(500L, provider.getRunTimeout());
    }

    @Test
    void testQueueTimeoutDefaultIsZero() {
        ThreadPoolStatProvider provider = createProvider();
        assertEquals(0L, provider.getQueueTimeout());
    }

    @Test
    void testSetAndGetQueueTimeout() {
        ThreadPoolStatProvider provider = createProvider();
        provider.setQueueTimeout(300L);
        assertEquals(300L, provider.getQueueTimeout());
    }

    @Test
    void testTryInterruptDefaultIsFalse() {
        ThreadPoolStatProvider provider = createProvider();
        assertFalse(provider.isTryInterrupt());
    }

    @Test
    void testSetTryInterrupt() {
        ThreadPoolStatProvider provider = createProvider();
        provider.setTryInterrupt(true);
        assertTrue(provider.isTryInterrupt());

        provider.setTryInterrupt(false);
        assertFalse(provider.isTryInterrupt());
    }

    @Test
    void testRejectCountInitiallyZero() {
        ThreadPoolStatProvider provider = createProvider();
        assertEquals(0L, provider.getRejectedTaskCount());
    }

    @Test
    void testIncRejectCount() {
        ThreadPoolStatProvider provider = createProvider();
        provider.incRejectCount(1);
        assertEquals(1L, provider.getRejectedTaskCount());
        provider.incRejectCount(4);
        assertEquals(5L, provider.getRejectedTaskCount());
    }

    @Test
    void testRunTimeoutCountInitiallyZero() {
        ThreadPoolStatProvider provider = createProvider();
        assertEquals(0L, provider.getRunTimeoutCount());
    }

    @Test
    void testIncRunTimeoutCount() {
        ThreadPoolStatProvider provider = createProvider();
        provider.incRunTimeoutCount(2);
        assertEquals(2L, provider.getRunTimeoutCount());
        provider.incRunTimeoutCount(3);
        assertEquals(5L, provider.getRunTimeoutCount());
    }

    @Test
    void testQueueTimeoutCountInitiallyZero() {
        ThreadPoolStatProvider provider = createProvider();
        assertEquals(0L, provider.getQueueTimeoutCount());
    }

    @Test
    void testIncQueueTimeoutCount() {
        ThreadPoolStatProvider provider = createProvider();
        provider.incQueueTimeoutCount(1);
        assertEquals(1L, provider.getQueueTimeoutCount());
        provider.incQueueTimeoutCount(9);
        assertEquals(10L, provider.getQueueTimeoutCount());
    }

    private ThreadPoolStatProvider createProvider() {
        dtpExecutor = new DtpExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        dtpExecutor.setThreadPoolName("stat-provider-test");
        ExecutorWrapper wrapper = new ExecutorWrapper(dtpExecutor);
        return wrapper.getThreadPoolStatProvider();
    }
}
