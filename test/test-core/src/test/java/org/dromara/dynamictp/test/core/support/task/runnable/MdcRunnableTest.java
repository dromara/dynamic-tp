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

package org.dromara.dynamictp.test.core.support.task.runnable;

import org.dromara.dynamictp.common.constant.DynamicTpConst;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.support.ThreadPoolBuilder;
import org.dromara.dynamictp.core.support.task.runnable.MdcRunnable;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.dromara.dynamictp.common.em.QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author kyao
 * @date 2023年09月21日 15:20
 */
@Slf4j
class MdcRunnableTest {

    private final String key = "key";

    private final String value = "value";

    private ExecutorService executor;

    @AfterEach
    void tearDown() {
        MDC.clear();
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    void test() throws InterruptedException {
        DtpExecutor executor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("dtpExecutor1")
                .corePoolSize(1)
                .maximumPoolSize(1)
                .keepAliveTime(15000)
                .timeUnit(TimeUnit.MILLISECONDS)
                .workQueue(VARIABLE_LINKED_BLOCKING_QUEUE.getName(), 20, false, null)
                .waitForTasksToCompleteOnShutdown(true)
                .awaitTerminationSeconds(5)
                .runTimeout(200)
                .queueTimeout(200)
                .buildDynamic();

        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(2);

        MDC.put(key, value);
        executor.execute(MdcRunnable.get(() -> {
            try {
                assertEquals(value, MDC.get(key));
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        }));

        MDC.remove(key);

        executor.execute(MdcRunnable.get(() -> {
            try {
                assertNull(MDC.get(key));
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        }));
        latch.await();
        if (error.get() != null) {
            throw new AssertionError("Assertion failed in worker thread", error.get());
        }
    }

    @Test
    void testReject() throws InterruptedException {
        DtpExecutor executor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("dtpExecutor1")
                .corePoolSize(1)
                .maximumPoolSize(1)
                .keepAliveTime(15000)
                .timeUnit(TimeUnit.MILLISECONDS)
                .workQueue(VARIABLE_LINKED_BLOCKING_QUEUE.getName(), 1, false, null)
                .waitForTasksToCompleteOnShutdown(true)
                .awaitTerminationSeconds(5)
                .runTimeout(200)
                .queueTimeout(200)
                .rejectedExecutionHandler("CallerRunsPolicy")
                .buildDynamic();

        AtomicReference<Throwable> rejectError = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(10);
        MDC.put(key, value);
        for (int i = 0; i < 10; i++) {
            try {
                executor.execute(MdcRunnable.get(() -> {
                    try {
                        Thread.sleep(300);
                        assertEquals(value, MDC.get(key));
                        log.info("value -> " + MDC.get(key));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Throwable t) {
                        rejectError.set(t);
                    } finally {
                        latch.countDown();
                    }
                }));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        latch.await();
        if (rejectError.get() != null) {
            throw new AssertionError("Assertion failed in worker thread", rejectError.get());
        }
        Assert.assertEquals(value, MDC.get(key));
        log.info("main thread value -> " + MDC.get(key));
    }

    @Test
    void testTraceIdIsPropagatedAndCustomEntriesAreCleared() throws InterruptedException {
        executor = Executors.newSingleThreadExecutor();
        MDC.put(DynamicTpConst.TRACE_ID, "trace-1");
        MDC.put(key, value);

        AtomicReference<String> childTraceId = new AtomicReference<>();
        AtomicReference<String> childCustomValue = new AtomicReference<>();
        AtomicReference<String> postRunTraceId = new AtomicReference<>();
        AtomicReference<String> postRunCustomValue = new AtomicReference<>();
        CountDownLatch firstRun = new CountDownLatch(1);
        CountDownLatch secondRun = new CountDownLatch(1);

        executor.submit(MdcRunnable.get(() -> {
            childTraceId.set(MDC.get(DynamicTpConst.TRACE_ID));
            childCustomValue.set(MDC.get(key));
            firstRun.countDown();
        }));
        Assert.assertTrue(firstRun.await(3, TimeUnit.SECONDS));

        executor.submit(() -> {
            postRunTraceId.set(MDC.get(DynamicTpConst.TRACE_ID));
            postRunCustomValue.set(MDC.get(key));
            secondRun.countDown();
        });
        Assert.assertTrue(secondRun.await(3, TimeUnit.SECONDS));

        assertEquals("trace-1", childTraceId.get());
        assertEquals(value, childCustomValue.get());
        assertEquals("trace-1", postRunTraceId.get());
        assertNull(postRunCustomValue.get());
    }

}
