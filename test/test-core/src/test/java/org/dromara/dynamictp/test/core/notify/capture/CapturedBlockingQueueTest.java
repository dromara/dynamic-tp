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

import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.notifier.capture.CapturedBlockingQueue;
import org.dromara.dynamictp.core.support.ThreadPoolBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                .buildDynamic();
    }

    @AfterAll
    public static void tearDown() {
        dtpExecutor.shutdownNow();
    }

    @Test
    public void testBlockingQueueDefaultCapacity() {
        CapturedBlockingQueue capturedBlockingQueue = new CapturedBlockingQueue(dtpExecutor);
        assertEquals(0, capturedBlockingQueue.size());
        assertEquals(100, capturedBlockingQueue.remainingCapacity());
        assertEquals(100, capturedBlockingQueue.getQueueCapacity());
        assertEquals("VariableLinkedBlockingQueue", capturedBlockingQueue.getQueueType());
        assertSame(dtpExecutor.getQueue(), capturedBlockingQueue.getOriginQueue());
    }

    @Test
    public void testPut() {
        CapturedBlockingQueue capturedBlockingQueue = new CapturedBlockingQueue(dtpExecutor);
        assertThrows(UnsupportedOperationException.class, () ->
                capturedBlockingQueue.put(() -> System.out.println("can't put Runnable to CapturedBlockingQueue")));
    }

    @Test
    public void testTake() {
        CapturedBlockingQueue capturedBlockingQueue = new CapturedBlockingQueue(dtpExecutor);
        assertThrows(UnsupportedOperationException.class, capturedBlockingQueue::take);
    }

    @Test
    public void testUnsupportedReadAndWriteOperations() {
        CapturedBlockingQueue capturedBlockingQueue = new CapturedBlockingQueue(dtpExecutor);

        assertThrows(UnsupportedOperationException.class, capturedBlockingQueue::iterator);
        assertThrows(UnsupportedOperationException.class, () -> capturedBlockingQueue.offer(() -> { }));
        assertThrows(UnsupportedOperationException.class, () ->
                capturedBlockingQueue.offer(() -> { }, 1, TimeUnit.MILLISECONDS));
        assertThrows(UnsupportedOperationException.class, capturedBlockingQueue::poll);
        assertThrows(UnsupportedOperationException.class, () -> capturedBlockingQueue.poll(1, TimeUnit.MILLISECONDS));
        assertThrows(UnsupportedOperationException.class, capturedBlockingQueue::peek);
        assertThrows(UnsupportedOperationException.class, () -> capturedBlockingQueue.drainTo(new ArrayList<>()));
        assertThrows(UnsupportedOperationException.class, () -> capturedBlockingQueue.drainTo(new ArrayList<>(), 1));
    }
}