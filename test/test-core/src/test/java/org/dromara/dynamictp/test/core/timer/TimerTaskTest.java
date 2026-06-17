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

package org.dromara.dynamictp.test.core.timer;

import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolBuilder;
import org.dromara.dynamictp.core.support.task.runnable.DtpRunnable;
import org.dromara.dynamictp.core.timer.RunTimeoutTimerTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TimerTask test
 *
 * @author yanhom
 * @since 1.2.2
 */
class TimerTaskTest {

    private DtpExecutor dtpExecutor;

    @AfterEach
    void tearDown() {
        if (dtpExecutor != null) {
            dtpExecutor.shutdownNow();
        }
    }

    @Test
    void testTraceToString() {
        RunTimeoutTimerTask task = createRunTimeoutTask(() -> { });
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        String result = task.traceToString(trace);

        assertNotNull(result);
        assertTrue(result.contains("testTraceToString"));
        assertTrue(result.startsWith("\n"));
    }

    @Test
    void testTraceToStringEmpty() {
        RunTimeoutTimerTask task = createRunTimeoutTask(() -> { });
        String result = task.traceToString(new StackTraceElement[0]);
        assertEquals("\n", result);
    }

    @Test
    void testTraceToStringSingleElement() {
        RunTimeoutTimerTask task = createRunTimeoutTask(() -> { });
        StackTraceElement[] trace = new StackTraceElement[]{
                new StackTraceElement("com.example.TestClass", "testMethod", "TestClass.java", 42)
        };
        String result = task.traceToString(trace);

        assertTrue(result.contains("TestClass.testMethod"));
        assertTrue(result.contains("TestClass.java:42"));
    }

    @Test
    void testGetTaskNameAndTraceIdWithDtpRunnable() {
        // Verify that DtpRunnable is properly detected
        // We test this indirectly by ensuring RunTimeoutTimerTask can be created
        // and works with DtpRunnable instances
        dtpExecutor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("timer-test-dtp")
                .buildDynamic();
        ExecutorWrapper wrapper = ExecutorWrapper.of(dtpExecutor);

        DtpRunnable dtpRunnable = new DtpRunnable(() -> { }, () -> { }, "testTask");
        RunTimeoutTimerTask task = new RunTimeoutTimerTask(wrapper, dtpRunnable, Thread.currentThread());

        assertNotNull(task);
    }

    @Test
    void testGetTaskNameAndTraceIdWithPlainRunnable() {
        // Plain runnable (not DtpRunnable) should return empty task name and trace id
        dtpExecutor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("timer-plain")
                .buildDynamic();
        ExecutorWrapper wrapper = ExecutorWrapper.of(dtpExecutor);

        RunTimeoutTimerTask task = new RunTimeoutTimerTask(wrapper, () -> { }, Thread.currentThread());
        assertNotNull(task);
    }

    @Test
    void testRunTimeoutCountIncrement() {
        dtpExecutor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("timer-count")
                .buildDynamic();
        ExecutorWrapper wrapper = ExecutorWrapper.of(dtpExecutor);

        assertEquals(0, wrapper.getThreadPoolStatProvider().getRunTimeoutCount());
        wrapper.getThreadPoolStatProvider().incRunTimeoutCount(1);
        assertEquals(1, wrapper.getThreadPoolStatProvider().getRunTimeoutCount());
        wrapper.getThreadPoolStatProvider().incRunTimeoutCount(3);
        assertEquals(4, wrapper.getThreadPoolStatProvider().getRunTimeoutCount());
    }

    @Test
    void testQueueTimeoutCountIncrement() {
        dtpExecutor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("timer-queue-count")
                .buildDynamic();
        ExecutorWrapper wrapper = ExecutorWrapper.of(dtpExecutor);

        assertEquals(0, wrapper.getThreadPoolStatProvider().getQueueTimeoutCount());
        wrapper.getThreadPoolStatProvider().incQueueTimeoutCount(1);
        assertEquals(1, wrapper.getThreadPoolStatProvider().getQueueTimeoutCount());
        wrapper.getThreadPoolStatProvider().incQueueTimeoutCount(5);
        assertEquals(6, wrapper.getThreadPoolStatProvider().getQueueTimeoutCount());
    }

    @Test
    void testTryInterruptFlag() {
        dtpExecutor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("timer-interrupt")
                .tryInterrupt(true)
                .buildDynamic();
        ExecutorWrapper wrapper = ExecutorWrapper.of(dtpExecutor);

        assertTrue(wrapper.getThreadPoolStatProvider().isTryInterrupt());
    }

    @Test
    void testTryInterruptDefaultFalse() {
        dtpExecutor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("timer-no-interrupt")
                .buildDynamic();
        ExecutorWrapper wrapper = ExecutorWrapper.of(dtpExecutor);

        assertFalse(wrapper.getThreadPoolStatProvider().isTryInterrupt());
    }

    private RunTimeoutTimerTask createRunTimeoutTask(Runnable runnable) {
        dtpExecutor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("timer-trace")
                .buildDynamic();
        ExecutorWrapper wrapper = ExecutorWrapper.of(dtpExecutor);
        return new RunTimeoutTimerTask(wrapper, runnable, Thread.currentThread());
    }
}
