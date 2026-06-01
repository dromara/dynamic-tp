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

package org.dromara.dynamictp.test.common.queue;

import org.dromara.dynamictp.common.queue.MemoryLimitCalculator;
import org.dromara.dynamictp.common.queue.MemorySafeLinkedBlockingQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * MemorySafeLinkedBlockingQueueTest related
 *
 * @author codex
 */
class MemorySafeLinkedBlockingQueueTest {

    @Test
    void testDefaultConstructorUsesDefaultMemoryLimit() {
        MemorySafeLinkedBlockingQueue<String> queue = new MemorySafeLinkedBlockingQueue<>();

        Assertions.assertEquals(MemorySafeLinkedBlockingQueue.THE_16_MB, queue.getMaxFreeMemory());
        Assertions.assertEquals(Integer.MAX_VALUE, queue.remainingCapacity() + queue.size());
    }

    @Test
    void testCapacityAndCollectionConstructorsKeepQueueState() {
        MemorySafeLinkedBlockingQueue<String> queue = new MemorySafeLinkedBlockingQueue<>(2, 1);
        MemorySafeLinkedBlockingQueue<String> collectionQueue =
                new MemorySafeLinkedBlockingQueue<>(Arrays.asList("a", "b"), 1);

        Assertions.assertEquals(2, queue.remainingCapacity() + queue.size());
        Assertions.assertEquals(2, collectionQueue.size());
        Assertions.assertEquals(1, collectionQueue.getMaxFreeMemory());
    }

    @Test
    void testOfferPutAndTimedOfferWhenMemoryRemains() throws InterruptedException {
        MemorySafeLinkedBlockingQueue<String> queue = new MemorySafeLinkedBlockingQueue<>(3, 1);

        Assertions.assertTrue(queue.offer("a"));
        queue.put("b");
        Assertions.assertTrue(queue.offer("c", 1, TimeUnit.MILLISECONDS));
        Assertions.assertEquals(3, queue.size());
    }

    @Test
    void testHasRemainedMemoryRejectsWhenLimitIsTooHigh() throws Exception {
        MemorySafeLinkedBlockingQueue<String> queue = new MemorySafeLinkedBlockingQueue<>();
        Field maxAvailable = MemoryLimitCalculator.class.getDeclaredField("maxAvailable");
        maxAvailable.setAccessible(true);
        long originMaxAvailable = maxAvailable.getLong(null);

        try {
            queue.setMaxFreeMemory(1);
            maxAvailable.setLong(null, 0);
            Assertions.assertThrows(RejectedExecutionException.class, queue::hasRemainedMemory);
        } finally {
            maxAvailable.setLong(null, originMaxAvailable);
        }
    }

    @Test
    void testMemoryLimitCalculatorInitializesAvailableMemory() {
        Assertions.assertTrue(MemoryLimitCalculator.maxAvailable() > 0);
    }
}
