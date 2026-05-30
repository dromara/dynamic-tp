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

import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.aware.RejectHandlerAware;
import org.dromara.dynamictp.core.aware.TaskEnhanceAware;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.notifier.capture.CapturedExecutor;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ExecutorWrapperTest related
 *
 * @author Copilot
 */
public class ExecutorWrapperTest {

    @Test
    public void testOfDtpExecutorCopiesCoreProperties() {
        DtpExecutor executor = mock(DtpExecutor.class);
        List<NotifyItem> notifyItems = Collections.singletonList(new NotifyItem());
        Set<String> awareNames = Collections.singleton("monitor");
        when(executor.getThreadPoolName()).thenReturn("dtpExecutor");
        when(executor.getThreadPoolAliasName()).thenReturn("aliasExecutor");
        when(executor.getNotifyItems()).thenReturn(notifyItems);
        when(executor.isNotifyEnabled()).thenReturn(false);
        when(executor.getPlatformIds()).thenReturn(Collections.singletonList("ding"));
        when(executor.getAwareNames()).thenReturn(awareNames);
        when(executor.isRejectEnhanced()).thenReturn(false);
        when(executor.isWaitForTasksToCompleteOnShutdown()).thenReturn(true);
        when(executor.getAwaitTerminationSeconds()).thenReturn(3);
        when(executor.getRunTimeout()).thenReturn(100L);
        when(executor.getQueueTimeout()).thenReturn(200L);
        when(executor.isTryInterrupt()).thenReturn(true);

        ExecutorWrapper wrapper = ExecutorWrapper.of(executor);

        Assert.assertSame(executor, wrapper.getExecutor());
        Assert.assertEquals("dtpExecutor", wrapper.getThreadPoolName());
        Assert.assertEquals("aliasExecutor", wrapper.getThreadPoolAliasName());
        Assert.assertSame(notifyItems, wrapper.getNotifyItems());
        Assert.assertFalse(wrapper.isNotifyEnabled());
        Assert.assertEquals(Collections.singletonList("ding"), wrapper.getPlatformIds());
        Assert.assertEquals(awareNames, wrapper.getAwareNames());
        Assert.assertFalse(wrapper.isRejectEnhanced());
        Assert.assertTrue(wrapper.isWaitForTasksToCompleteOnShutdown());
        Assert.assertEquals(3, wrapper.getAwaitTerminationSeconds());
        Assert.assertEquals(100L, wrapper.getThreadPoolStatProvider().getRunTimeout());
        Assert.assertEquals(200L, wrapper.getThreadPoolStatProvider().getQueueTimeout());
        Assert.assertTrue(wrapper.getThreadPoolStatProvider().isTryInterrupt());
    }

    @Test
    public void testCaptureWrapsExecutorSnapshot() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(8));
        ExecutorWrapper wrapper = newExecutorWrapper("capturedPool", executor);

        ExecutorWrapper captured = wrapper.capture();

        Assert.assertTrue(captured.getExecutor() instanceof CapturedExecutor);
        Assert.assertEquals("capturedPool", captured.getThreadPoolName());
        Assert.assertSame(wrapper.getNotifyItems(), captured.getNotifyItems());
        Assert.assertSame(wrapper.getPlatformIds(), captured.getPlatformIds());
        executor.shutdownNow();
    }

    @Test
    public void testInitializeInvokesExecutorAndAwareManagerForDtpExecutor() {
        DtpExecutor executor = mock(DtpExecutor.class);
        ExecutorWrapper wrapper = ExecutorWrapper.of(executor);

        try (MockedStatic<AwareManager> awareManagerMock = Mockito.mockStatic(AwareManager.class)) {
            wrapper.initialize();

            verify(executor).initialize();
            awareManagerMock.verify(() -> AwareManager.register(wrapper));
        }
    }

    @Test
    public void testSetTaskWrappersAndRejectHandlerDelegateToAwareExecutor() {
        TestAwareThreadPoolExecutor executor = new TestAwareThreadPoolExecutor();
        ExecutorWrapper wrapper = newExecutorWrapper("awarePool", executor);
        List<TaskWrapper> taskWrappers = Collections.singletonList(mock(TaskWrapper.class));
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();

        wrapper.setTaskWrappers(taskWrappers);
        wrapper.setRejectHandler(handler);

        Assert.assertSame(taskWrappers, executor.getTaskWrappers());
        Assert.assertEquals("AbortPolicy", executor.getRejectHandlerType());
        Assert.assertNotSame(handler, executor.getRejectedExecutionHandler());
    }

    @Test
    public void testSetRejectHandlerKeepsOriginalHandlerWhenRejectEnhanceDisabled() {
        TestAwareThreadPoolExecutor executor = new TestAwareThreadPoolExecutor();
        ExecutorWrapper wrapper = newExecutorWrapper("awarePool", executor);
        wrapper.setRejectEnhanced(false);
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();

        wrapper.setRejectHandler(handler);

        Assert.assertSame(handler, executor.getRejectedExecutionHandler());
        Assert.assertEquals("CallerRunsPolicy", executor.getRejectHandlerType());
    }

    @Test
    public void testUnsupportedExecutorTypeThrowsException() {
        Assert.assertThrows(IllegalArgumentException.class,
                () -> new ExecutorWrapper("unsupported", command -> { }));
    }

    private ExecutorWrapper newExecutorWrapper(String threadPoolName, ThreadPoolExecutor executor) {
        try {
            Constructor<ExecutorWrapper> constructor = ExecutorWrapper.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            ExecutorWrapper wrapper = constructor.newInstance();
            setField(wrapper, "threadPoolName", threadPoolName);
            setField(wrapper, "executor", executor instanceof DtpExecutor ? executor :
                    new org.dromara.dynamictp.core.support.adapter.ThreadPoolExecutorAdapter(executor));
            return wrapper;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(ExecutorWrapper wrapper, String fieldName, Object value) throws Exception {
        Field field = ExecutorWrapper.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(wrapper, value);
    }

    private static class TestAwareThreadPoolExecutor extends ThreadPoolExecutor implements TaskEnhanceAware, RejectHandlerAware {

        private List<TaskWrapper> taskWrappers;

        private String rejectHandlerType;

        TestAwareThreadPoolExecutor() {
            super(1, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        }

        @Override
        public List<TaskWrapper> getTaskWrappers() {
            return taskWrappers;
        }

        @Override
        public void setTaskWrappers(List<TaskWrapper> taskWrappers) {
            this.taskWrappers = taskWrappers;
        }

        @Override
        public String getRejectHandlerType() {
            return rejectHandlerType;
        }

        @Override
        public void setRejectHandlerType(String rejectHandlerType) {
            this.rejectHandlerType = rejectHandlerType;
        }
    }
}
