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

import org.dromara.dynamictp.core.aware.ExecutorAware;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static org.mockito.Mockito.*;

/**
 * AwareManagerTest related
 *
 * @author vzer200
 * @since 1.1.8
 */
class AwareManagerTest {

    private ExecutorWrapper executorWrapper;
    private ExecutorAware mockAware1;
    private ExecutorAware mockAware2;
    private Executor mockExecutor;
    private Runnable mockRunnable;

    private List<ExecutorAware> customAwareList;

    @BeforeEach
    void setUp() {
        executorWrapper = mock(ExecutorWrapper.class);
        mockAware1 = mock(ExecutorAware.class);
        mockAware2 = mock(ExecutorAware.class);
        mockExecutor = mock(Executor.class);
        mockRunnable = mock(Runnable.class);

        when(mockAware1.getName()).thenReturn("Aware1");
        when(mockAware2.getName()).thenReturn("Aware2");

        customAwareList = new ArrayList<>();
        customAwareList.add(mockAware1);
        customAwareList.add(mockAware2);
    }

    @Test
    void testRegister() {
        for (ExecutorAware aware : customAwareList) {
            aware.register(executorWrapper);
        }

        verify(mockAware1).register(executorWrapper);
        verify(mockAware2).register(executorWrapper);
    }

    @Test
    void testExecute() {
        for (ExecutorAware aware : customAwareList) {
            aware.execute(mockExecutor, mockRunnable);
        }

        verify(mockAware1).execute(mockExecutor, mockRunnable);
        verify(mockAware2).execute(mockExecutor, mockRunnable);
    }

    @Test
    void testBeforeExecute() {
        Thread mockThread = mock(Thread.class);
        for (ExecutorAware aware : customAwareList) {
            aware.beforeExecuteWrap(mockExecutor, mockThread, mockRunnable);
        }

        verify(mockAware1).beforeExecuteWrap(mockExecutor, mockThread, mockRunnable);
        verify(mockAware2).beforeExecuteWrap(mockExecutor, mockThread, mockRunnable);
    }

    @Test
    void testAfterExecute() {
        for (ExecutorAware aware : customAwareList) {
            aware.afterExecuteWrap(mockExecutor, mockRunnable, null);
        }

        verify(mockAware1).afterExecuteWrap(mockExecutor, mockRunnable, null);
        verify(mockAware2).afterExecuteWrap(mockExecutor, mockRunnable, null);
    }

    @Test
    void testShutdown() {
        for (ExecutorAware aware : customAwareList) {
            aware.shutdown(mockExecutor);
        }

        verify(mockAware1).shutdown(mockExecutor);
        verify(mockAware2).shutdown(mockExecutor);
    }

    @Test
    void testShutdownNow() {
        List<Runnable> mockTasks = new ArrayList<>();
        for (ExecutorAware aware : customAwareList) {
            aware.shutdownNow(mockExecutor, mockTasks);
        }

        verify(mockAware1).shutdownNow(mockExecutor, mockTasks);
        verify(mockAware2).shutdownNow(mockExecutor, mockTasks);
    }
}
