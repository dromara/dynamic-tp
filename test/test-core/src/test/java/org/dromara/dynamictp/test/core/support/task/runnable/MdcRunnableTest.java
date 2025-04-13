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

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.support.ThreadPoolBuilder;
import org.dromara.dynamictp.core.support.task.runnable.MdcRunnable;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.MDC;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.dromara.dynamictp.common.em.QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE;

/**
 * @author kyao
 * @date 2023年09月21日 15:20
 */
@Slf4j
public class MdcRunnableTest {

    private final String key = "key";

    private final String value = "value";

    @Test
    public void test() throws InterruptedException {
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

        CountDownLatch latch = new CountDownLatch(2);

        MDC.put(key, value);
        executor.execute(MdcRunnable.get(() -> {
            Assert.assertEquals(value, MDC.get(key));
            latch.countDown();

        }));

        MDC.remove(key);

        executor.execute(MdcRunnable.get(() -> {
            Assert.assertNull(MDC.get(key));
            latch.countDown();
        }));
        latch.await();
    }

    @Test
    public void testReject() throws InterruptedException {
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

        CountDownLatch latch = new CountDownLatch(10);
        MDC.put(key, value);
        for (int i = 0; i < 10; i++) {
            try {
                executor.execute(MdcRunnable.get(() -> {
                    try {
                        Thread.sleep(300);
                        Assert.assertEquals(value, MDC.get(key));
                        log.info("value -> " + MDC.get(key));
                        latch.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        latch.await();
        Assert.assertEquals(value, MDC.get(key));
        log.info("main thread value -> " + MDC.get(key));
    }

}
