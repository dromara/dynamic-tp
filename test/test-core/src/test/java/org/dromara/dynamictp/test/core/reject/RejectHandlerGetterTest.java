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

package org.dromara.dynamictp.test.core.reject;

import org.dromara.dynamictp.common.ex.DtpException;
import org.dromara.dynamictp.core.reject.RejectHandlerGetter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RejectHandlerGetter test
 *
 * @author yanhom
 * @since 1.2.2
 */
class RejectHandlerGetterTest {

    @Test
    void testBuildAbortPolicy() {
        RejectedExecutionHandler handler = RejectHandlerGetter.buildRejectedHandler("AbortPolicy");
        assertNotNull(handler);
        assertInstanceOf(ThreadPoolExecutor.AbortPolicy.class, handler);
    }

    @Test
    void testBuildCallerRunsPolicy() {
        RejectedExecutionHandler handler = RejectHandlerGetter.buildRejectedHandler("CallerRunsPolicy");
        assertNotNull(handler);
        assertInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class, handler);
    }

    @Test
    void testBuildDiscardOldestPolicy() {
        RejectedExecutionHandler handler = RejectHandlerGetter.buildRejectedHandler("DiscardOldestPolicy");
        assertNotNull(handler);
        assertInstanceOf(ThreadPoolExecutor.DiscardOldestPolicy.class, handler);
    }

    @Test
    void testBuildDiscardPolicy() {
        RejectedExecutionHandler handler = RejectHandlerGetter.buildRejectedHandler("DiscardPolicy");
        assertNotNull(handler);
        assertInstanceOf(ThreadPoolExecutor.DiscardPolicy.class, handler);
    }

    @Test
    void testBuildUnknownHandlerThrowsException() {
        DtpException ex = assertThrows(DtpException.class,
                () -> RejectHandlerGetter.buildRejectedHandler("UnknownPolicy"));
        assertTrue(ex.getMessage().contains("UnknownPolicy"));
    }

    @Test
    void testGetProxyByNameReturnsProxy() {
        RejectedExecutionHandler proxy = RejectHandlerGetter.getProxy("AbortPolicy");
        assertNotNull(proxy);
        // proxy should not be the same type as the raw handler, it's a JDK dynamic proxy
        assertTrue(java.lang.reflect.Proxy.isProxyClass(proxy.getClass()));
    }

    @Test
    void testGetProxyByHandlerReturnsProxy() {
        RejectedExecutionHandler raw = new ThreadPoolExecutor.AbortPolicy();
        RejectedExecutionHandler proxy = RejectHandlerGetter.getProxy(raw);
        assertNotNull(proxy);
        assertTrue(java.lang.reflect.Proxy.isProxyClass(proxy.getClass()));
    }

    @Test
    void testProxyInvocationDelegatesToTarget() {
        // CallerRunsPolicy simply runs the task in the caller thread
        RejectedExecutionHandler proxy = RejectHandlerGetter.getProxy("CallerRunsPolicy");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, 1, 0, java.util.concurrent.TimeUnit.SECONDS,
                new java.util.concurrent.SynchronousQueue<>());
        Runnable task = mockRejectedTask();
        // The proxy should invoke the underlying CallerRunsPolicy which runs task in caller thread
        assertDoesNotThrow(() -> proxy.rejectedExecution(task, executor));
        executor.shutdownNow();
    }

    private static Runnable mockRejectedTask() {
        return () -> { };
    }

    private static void assertDoesNotThrow(org.junit.jupiter.api.function.Executable executable) {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(executable);
    }
}
