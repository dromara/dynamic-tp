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

package org.dromara.dynamictp.test.core.notify.capture;

import org.dromara.dynamictp.core.notifier.capture.CapturedExecutor;
import org.dromara.dynamictp.core.support.adapter.ExecutorAdapter;
import org.dromara.dynamictp.core.support.ThreadPoolBuilder;
import org.dromara.dynamictp.core.support.ThreadPoolCreator;
import org.dromara.dynamictp.core.support.adapter.ThreadPoolExecutorAdapter;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.dromara.dynamictp.common.em.QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE;

/**
 * CapturedExecutorTest related
 *
 * @author ruoan
 * @since 1.1.3
 */
public class CapturedExecutorTest {

    private static DtpExecutor dtpExecutor;

    @BeforeAll
    public static void setUp() {
        dtpExecutor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("dtpExecutor1")
                .corePoolSize(10)
                .maximumPoolSize(15)
                .keepAliveTime(15000)
                .timeUnit(TimeUnit.MILLISECONDS)
                .workQueue(VARIABLE_LINKED_BLOCKING_QUEUE.getName(), 100, false, null)
                .waitForTasksToCompleteOnShutdown(true)
                .awaitTerminationSeconds(5)
                .runTimeout(200)
                .queueTimeout(200)
                .buildDynamic();
    }

    @AfterAll
    public static void tearDown() {
        dtpExecutor.shutdownNow();
    }

    @Test
    public void testCapturedExecutor() {
        CapturedExecutor capturedExecutor = new CapturedExecutor(dtpExecutor);
        assertEquals(dtpExecutor, capturedExecutor.getOriginal());
        assertEquals(capturedExecutor.getCorePoolSize(), 10);
        assertEquals(capturedExecutor.getMaximumPoolSize(), 15);
        assertEquals(capturedExecutor.getPoolSize(), 0);
        assertEquals(capturedExecutor.getActiveCount(), 0);
        assertEquals(capturedExecutor.getTaskCount(), 0);
        assertEquals(capturedExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS), 15000);
        assertEquals(capturedExecutor.getQueueRemainingCapacity(), 100);
        assertEquals(capturedExecutor.getQueueCapacity(), 100);
        assertEquals(capturedExecutor.getQueueType(), "VariableLinkedBlockingQueue");
        assertEquals(capturedExecutor.getRejectHandlerType(), dtpExecutor.getRejectHandlerType());
    }

    @Test
    public void testSetCorePoolSize() {
        ExecutorAdapter<?> executor = dtpExecutor;
        CapturedExecutor captured = new CapturedExecutor(executor);
        assertThrows(UnsupportedOperationException.class, () ->
                captured.setCorePoolSize(10));
    }

    @Test
    public void testExecute() {
        ExecutorAdapter<?> executor = dtpExecutor;
        CapturedExecutor captured = new CapturedExecutor(executor);
        assertThrows(UnsupportedOperationException.class, () ->
                captured.execute(() -> System.out.println("i am mock task")));
    }

    @Test
    public void testUnsupportedMutators() {
        CapturedExecutor captured = new CapturedExecutor(dtpExecutor);

        assertThrows(UnsupportedOperationException.class, () -> captured.setMaximumPoolSize(20));
        assertThrows(UnsupportedOperationException.class, () -> captured.setRejectedExecutionHandler(
                new ThreadPoolExecutor.AbortPolicy()));
        assertThrows(UnsupportedOperationException.class, () -> captured.allowCoreThreadTimeOut(true));
        assertThrows(UnsupportedOperationException.class, () -> captured.setKeepAliveTime(10, TimeUnit.SECONDS));
    }

    @Test
    public void testGetActiveCount() {
        ThreadPoolExecutor threadPoolExecutor = ThreadPoolCreator.createCommonFast("test");
        threadPoolExecutor.execute(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        });
        CapturedExecutor captured = new CapturedExecutor(new ThreadPoolExecutorAdapter(threadPoolExecutor));
        assertEquals(1, captured.getActiveCount());
        threadPoolExecutor.shutdownNow();
    }

    @Test
    public void testGetCompletedTaskCount() throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = ThreadPoolCreator.createCommonFast("test");

        executeSomeTask(threadPoolExecutor);
        TimeUnit.MILLISECONDS.sleep(100);

        CapturedExecutor captured = new CapturedExecutor(new ThreadPoolExecutorAdapter(threadPoolExecutor));
        assertEquals(10, captured.getCompletedTaskCount());
        threadPoolExecutor.shutdownNow();
    }

    @RepeatedTest(50)
    public void testGetTaskCount() throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = ThreadPoolCreator.createCommonFast("test");

        executeSomeTask(threadPoolExecutor);
        TimeUnit.MILLISECONDS.sleep(100);

        CapturedExecutor captured = new CapturedExecutor(new ThreadPoolExecutorAdapter(threadPoolExecutor));
        assertEquals(10, captured.getTaskCount());
        assertEquals(10, threadPoolExecutor.getTaskCount());

        executeSomeTask(threadPoolExecutor);
        TimeUnit.MILLISECONDS.sleep(100);

        //The status of CapturedExecutor remains unchanged after creation.
        assertEquals(10, captured.getTaskCount());
        assertNotEquals(10, threadPoolExecutor.getTaskCount());
        threadPoolExecutor.shutdownNow();
    }

    private void executeSomeTask(ThreadPoolExecutor threadPoolExecutor) {
        for (int i = 0; i < 10; i++) {
            threadPoolExecutor.execute(() -> {
                //do nothing
            });
        }
    }
}
