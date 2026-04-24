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

package org.dromara.dynamictp.test.core.aware;

import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.aware.ExecutorAware;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolStatProvider;
import org.dromara.dynamictp.core.support.adapter.ExecutorAdapter;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AwareManagerTest related
 *
 * @author vzer200
 * @since 1.1.8
 */
class AwareManagerTest {

    private ExecutorAware mockAware(String name, int order) {
        ExecutorAware aware = mock(ExecutorAware.class);
        when(aware.getName()).thenReturn(name);
        when(aware.getOrder()).thenReturn(order);
        return aware;
    }

    /**
     * Build a mock ExecutorWrapper that satisfies TaskStatAware.register() /
     * refresh() / remove() which call wrapper.getExecutor() / getOriginal() /
     * getThreadPoolStatProvider().
     */
    private ExecutorWrapper mockWrapper(Set<String> awareNames) {
        ExecutorAdapter<?> executorAdapter = mock(ExecutorAdapter.class);
        Executor original = mock(Executor.class);
        doReturn(original).when(executorAdapter).getOriginal();

        ThreadPoolStatProvider statProvider = mock(ThreadPoolStatProvider.class);

        ExecutorWrapper wrapper = mock(ExecutorWrapper.class);
        when(wrapper.getAwareNames()).thenReturn(awareNames);
        when(wrapper.getThreadPoolStatProvider()).thenReturn(statProvider);
        return wrapper;
    }

    // ==================== add ====================

    @Test
    void testAddDuplicateAwareShouldBeIdempotent() {
        ExecutorAware aware1 = mockAware("TestDupAware", 0);
        AwareManager.add(aware1);

        ExecutorAware aware2 = mockAware("TestDupAware", 1);
        // Same name, should be silently ignored
        assertDoesNotThrow(() -> AwareManager.add(aware2));
    }

    // ==================== lifecycle callbacks ====================

    @Test
    void testExecuteCallback() {
        Executor executor = mock(Executor.class);
        Runnable runnable = mock(Runnable.class);

        assertDoesNotThrow(() -> AwareManager.execute(executor, runnable));
    }

    @Test
    void testBeforeExecuteCallback() {
        Executor executor = mock(Executor.class);
        Thread thread = mock(Thread.class);
        Runnable runnable = mock(Runnable.class);

        assertDoesNotThrow(() -> AwareManager.beforeExecute(executor, thread, runnable));
    }

    @Test
    void testAfterExecuteCallback() {
        Executor executor = mock(Executor.class);
        Runnable runnable = mock(Runnable.class);
        Throwable throwable = new RuntimeException("test");

        assertDoesNotThrow(() -> AwareManager.afterExecute(executor, runnable, throwable));
    }

    @Test
    void testShutdownCallback() {
        Executor executor = mock(Executor.class);
        assertDoesNotThrow(() -> AwareManager.shutdown(executor));
    }

    @Test
    void testTerminatedCallback() {
        Executor executor = mock(Executor.class);
        assertDoesNotThrow(() -> AwareManager.terminated(executor));
    }

    @Test
    void testBeforeRejectCallback() {
        Runnable runnable = mock(Runnable.class);
        Executor executor = mock(Executor.class);

        assertDoesNotThrow(() -> AwareManager.beforeReject(runnable, executor));
    }

    @Test
    void testAfterRejectCallback() {
        Runnable runnable = mock(Runnable.class);
        Executor executor = mock(Executor.class);

        assertDoesNotThrow(() -> AwareManager.afterReject(runnable, executor));
    }

    // ==================== exception tolerance ====================

    @Test
    void testExecuteCallbackToleratesAwareException() {
        ExecutorAware faultyAware = mockAware("FaultyExecuteAware", 0);
        doThrow(new RuntimeException("intentional")).when(faultyAware).execute(any(), any());
        AwareManager.add(faultyAware);

        Executor executor = mock(Executor.class);
        Runnable runnable = mock(Runnable.class);

        assertDoesNotThrow(() -> AwareManager.execute(executor, runnable));
    }
}
