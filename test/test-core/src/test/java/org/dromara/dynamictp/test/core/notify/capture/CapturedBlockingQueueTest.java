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

import org.dromara.dynamictp.common.queue.VariableLinkedBlockingQueue;
import org.dromara.dynamictp.core.notifier.capture.CapturedBlockingQueue;
import org.dromara.dynamictp.core.support.ThreadPoolBuilder;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.dromara.dynamictp.common.em.QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE;

/**
 * CapturedBlockingQueueTest related
 *
 * @author ruoan
 * @since 1.1.3
 */
public class CapturedBlockingQueueTest {

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

    @Test
    public void testBlockingQueueDefaultCapacity() {
        CapturedBlockingQueue capturedBlockingQueue = new CapturedBlockingQueue(dtpExecutor);
        Assertions.assertEquals(0, capturedBlockingQueue.size());
        Assertions.assertEquals(Integer.MAX_VALUE, capturedBlockingQueue.remainingCapacity());
    }

    @Test
    public void testBlockingQueueCapacitySet() {
        final int capacity = 100;
        BlockingQueue<Runnable> queue = new VariableLinkedBlockingQueue<>(capacity);
        CapturedBlockingQueue capturedBlockingQueue = new CapturedBlockingQueue(dtpExecutor);
        Assertions.assertEquals(capacity, capturedBlockingQueue.remainingCapacity());
    }

    @Test
    public void testPut() {
        BlockingQueue<Runnable> queue = new VariableLinkedBlockingQueue<>();
        CapturedBlockingQueue capturedBlockingQueue = new CapturedBlockingQueue(dtpExecutor);
        Assertions.assertThrows(UnsupportedOperationException.class, () ->
                capturedBlockingQueue.put(() -> System.out.println("can't put Runnable to CapturedBlockingQueue")));
    }

    @Test
    public void testTake() {
        BlockingQueue<Runnable> queue = new VariableLinkedBlockingQueue<>();
        CapturedBlockingQueue capturedBlockingQueue = new CapturedBlockingQueue(dtpExecutor);
        Assertions.assertThrows(UnsupportedOperationException.class, capturedBlockingQueue::take);
    }

    @RepeatedTest(100)
    public void testBlockingQueuePut() throws InterruptedException {
        final int capacity = 100;
        final int firstPutSize = 30;
        final int secondPutSize = 40;

        BlockingQueue<Runnable> queue = new VariableLinkedBlockingQueue<>(capacity);
        putTaskToQueue(firstPutSize, queue);
        CapturedBlockingQueue capturedBlockingQueue = new CapturedBlockingQueue(dtpExecutor);

        Assertions.assertEquals(capacity - firstPutSize, capturedBlockingQueue.remainingCapacity());
        Assertions.assertEquals(firstPutSize, capturedBlockingQueue.size());

        putTaskToQueue(secondPutSize, queue);

        //The status of VariableLinkedBlockingQueue changes dynamically as tasks are added to it.
        Assertions.assertEquals(capacity - firstPutSize - secondPutSize, queue.remainingCapacity());
        Assertions.assertEquals(firstPutSize + secondPutSize, queue.size());

        //The status of CapturedBlockingQueue remains unchanged after creation.
        Assertions.assertEquals(capacity - firstPutSize, capturedBlockingQueue.remainingCapacity());
        Assertions.assertEquals(firstPutSize, capturedBlockingQueue.size());
    }

    private void putTaskToQueue(int size, BlockingQueue<Runnable> queue) throws InterruptedException {
        for (int i = 0; i < size; i++) {
            queue.put(() -> System.out.println("i am mock task"));
        }
    }
}
