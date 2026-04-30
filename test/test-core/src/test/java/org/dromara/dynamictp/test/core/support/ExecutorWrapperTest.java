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
import org.dromara.dynamictp.core.notifier.capture.CapturedExecutor;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ExecutorWrapper test
 *
 * @author yanhom
 * @since 1.2.2
 */
class ExecutorWrapperTest {

    private DtpExecutor dtpExecutor;

    @AfterEach
    void tearDown() {
        if (dtpExecutor != null) {
            dtpExecutor.shutdownNow();
        }
    }

    @Test
    void testConstructorWithDtpExecutor() {
        dtpExecutor = new DtpExecutor(2, 4, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        dtpExecutor.setThreadPoolName("test-dtp");

        ExecutorWrapper wrapper = new ExecutorWrapper(dtpExecutor);

        assertEquals("test-dtp", wrapper.getThreadPoolName());
        assertTrue(wrapper.isDtpExecutor());
        assertFalse(wrapper.isThreadPoolExecutor());
        assertNotNull(wrapper.getThreadPoolStatProvider());
        assertTrue(wrapper.isNotifyEnabled());
        assertTrue(wrapper.isRejectEnhanced());
    }

    @Test
    void testConstructorPreservesAliasName() {
        dtpExecutor = new DtpExecutor(2, 4, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        dtpExecutor.setThreadPoolName("dtp-alias");
        dtpExecutor.setThreadPoolAliasName("alias-test");

        ExecutorWrapper wrapper = new ExecutorWrapper(dtpExecutor);

        assertEquals("dtp-alias", wrapper.getThreadPoolName());
        assertEquals("alias-test", wrapper.getThreadPoolAliasName());
    }

    @Test
    void testConstructorPreservesNotifyEnabled() {
        dtpExecutor = new DtpExecutor(2, 4, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        dtpExecutor.setThreadPoolName("notify-dtp");
        dtpExecutor.setNotifyEnabled(false);

        ExecutorWrapper wrapper = new ExecutorWrapper(dtpExecutor);

        assertFalse(wrapper.isNotifyEnabled());
    }

    @Test
    void testConstructorPreservesShutdownConfig() {
        dtpExecutor = new DtpExecutor(2, 4, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        dtpExecutor.setThreadPoolName("shutdown-dtp");
        dtpExecutor.setWaitForTasksToCompleteOnShutdown(true);
        dtpExecutor.setAwaitTerminationSeconds(60);

        ExecutorWrapper wrapper = new ExecutorWrapper(dtpExecutor);

        assertTrue(wrapper.isWaitForTasksToCompleteOnShutdown());
        assertEquals(60, wrapper.getAwaitTerminationSeconds());
    }

    @Test
    void testOfFactoryMethod() {
        dtpExecutor = new DtpExecutor(2, 4, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        dtpExecutor.setThreadPoolName("factory-dtp");

        ExecutorWrapper wrapper = ExecutorWrapper.of(dtpExecutor);

        assertEquals("factory-dtp", wrapper.getThreadPoolName());
        assertTrue(wrapper.isDtpExecutor());
    }

    @Test
    void testIsExecutorServiceReturnsTrue() {
        dtpExecutor = new DtpExecutor(2, 4, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        ExecutorWrapper wrapper = new ExecutorWrapper(dtpExecutor);

        assertTrue(wrapper.isExecutorService());
    }

    @Test
    void testCaptureReturnsSnapshot() {
        dtpExecutor = new DtpExecutor(2, 4, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        dtpExecutor.setThreadPoolName("capture-dtp");

        ExecutorWrapper original = new ExecutorWrapper(dtpExecutor);
        ExecutorWrapper captured = original.capture();

        assertEquals(original.getThreadPoolName(), captured.getThreadPoolName());
        assertNotNull(captured.getExecutor());
        assertInstanceOf(CapturedExecutor.class, captured.getExecutor());
        // captured executor is a different object
        assertNotSame(original.getExecutor(), captured.getExecutor());
    }

    @Test
    void testCapturePreservesNotifyConfig() {
        dtpExecutor = new DtpExecutor(2, 4, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        dtpExecutor.setThreadPoolName("capture-notify");
        dtpExecutor.setNotifyEnabled(false);

        ExecutorWrapper original = new ExecutorWrapper(dtpExecutor);
        ExecutorWrapper captured = original.capture();

        assertFalse(captured.isNotifyEnabled());
        assertEquals(original.getThreadPoolName(), captured.getThreadPoolName());
    }

    @Test
    void testSetRejectHandlerWithEnhanced() {
        dtpExecutor = new DtpExecutor(2, 4, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        dtpExecutor.setThreadPoolName("reject-enhanced");

        ExecutorWrapper wrapper = new ExecutorWrapper(dtpExecutor);
        assertTrue(wrapper.isRejectEnhanced());

        wrapper.setRejectHandler(new ThreadPoolExecutor.AbortPolicy());
        // with enhancement, handler should be a JDK dynamic proxy
        assertTrue(java.lang.reflect.Proxy.isProxyClass(
                dtpExecutor.getRejectedExecutionHandler().getClass()));
    }

    @Test
    void testSetRejectHandlerWithoutEnhanced() {
        dtpExecutor = new DtpExecutor(2, 4, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        dtpExecutor.setThreadPoolName("reject-plain");

        ExecutorWrapper wrapper = new ExecutorWrapper(dtpExecutor);
        wrapper.setRejectEnhanced(false);

        wrapper.setRejectHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // without enhancement, handler is set directly (not a proxy)
        assertFalse(java.lang.reflect.Proxy.isProxyClass(
                dtpExecutor.getRejectedExecutionHandler().getClass()));
    }
}
