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

import org.dromara.dynamictp.core.reject.RejectedInvocationHandler;
import org.junit.jupiter.api.Test;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * RejectedInvocationHandler test
 *
 * @author yanhom
 * @since 1.2.2
 */
class RejectedInvocationHandlerTest {

    @Test
    void testInvokeDelegatesToTarget() throws Throwable {
        RejectedExecutionHandler target = mock(RejectedExecutionHandler.class);
        RejectedInvocationHandler handler = new RejectedInvocationHandler(target);

        ThreadPoolExecutor executor = mock(ThreadPoolExecutor.class);
        Runnable runnable = mock(Runnable.class);

        handler.invoke(null,
                RejectedExecutionHandler.class.getMethod("rejectedExecution", Runnable.class, ThreadPoolExecutor.class),
                new Object[]{runnable, executor});

        verify(target).rejectedExecution(runnable, executor);
    }

    @Test
    void testInvokeWithAwareCallbacks() throws Throwable {
        RejectedExecutionHandler target = mock(RejectedExecutionHandler.class);
        RejectedInvocationHandler handler = new RejectedInvocationHandler(target);

        ThreadPoolExecutor executor = mock(ThreadPoolExecutor.class);
        Runnable runnable = mock(Runnable.class);

        // AwareManager.beforeReject / afterReject are called, but no awares registered => no exception
        assertDoesNotThrow(() -> handler.invoke(null,
                RejectedExecutionHandler.class.getMethod("rejectedExecution", Runnable.class, ThreadPoolExecutor.class),
                new Object[]{runnable, executor}));

        verify(target).rejectedExecution(runnable, executor);
    }
}
