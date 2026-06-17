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

package org.dromara.dynamictp.test.common.em;

import org.dromara.dynamictp.common.em.QueueTypeEnum;
import org.dromara.dynamictp.common.ex.DtpException;
import org.dromara.dynamictp.common.queue.MemorySafeLinkedBlockingQueue;
import org.dromara.dynamictp.common.queue.VariableLinkedBlockingQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * QueueTypeEnumTest related.
 */
class QueueTypeEnumTest {

    @Test
    void testBuildLbqReturnsExpectedQueueTypes() {
        Assertions.assertInstanceOf(ArrayBlockingQueue.class,
                QueueTypeEnum.buildLbq(QueueTypeEnum.ARRAY_BLOCKING_QUEUE.getName(), 2));
        Assertions.assertInstanceOf(LinkedBlockingQueue.class,
                QueueTypeEnum.buildLbq(QueueTypeEnum.LINKED_BLOCKING_QUEUE.getName(), 2));
        Assertions.assertInstanceOf(PriorityBlockingQueue.class,
                QueueTypeEnum.buildLbq(QueueTypeEnum.PRIORITY_BLOCKING_QUEUE.getName(), 2));
        Assertions.assertInstanceOf(DelayQueue.class,
                QueueTypeEnum.buildLbq(QueueTypeEnum.DELAY_QUEUE.getName(), 2));
        Assertions.assertInstanceOf(LinkedTransferQueue.class,
                QueueTypeEnum.buildLbq(QueueTypeEnum.LINKED_TRANSFER_QUEUE.getName(), 2));
        Assertions.assertInstanceOf(LinkedBlockingDeque.class,
                QueueTypeEnum.buildLbq(QueueTypeEnum.LINKED_BLOCKING_DEQUE.getName(), 2));
        Assertions.assertInstanceOf(VariableLinkedBlockingQueue.class,
                QueueTypeEnum.buildLbq(QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE.getName(), 2));
        Assertions.assertInstanceOf(MemorySafeLinkedBlockingQueue.class,
                QueueTypeEnum.buildLbq(QueueTypeEnum.MEMORY_SAFE_LINKED_BLOCKING_QUEUE.getName(), 2));
    }

    @Test
    void testBuildLbqCreatesFairSynchronousQueue() {
        BlockingQueue<Runnable> queue = QueueTypeEnum.buildLbq(
                QueueTypeEnum.SYNCHRONOUS_QUEUE.getName(), 2, true, 256);

        Assertions.assertInstanceOf(SynchronousQueue.class, queue);
        Assertions.assertEquals(0, queue.remainingCapacity());
    }

    @Test
    void testBuildLbqThrowsWhenNameIsUnknown() {
        DtpException exception = Assertions.assertThrows(DtpException.class,
                () -> QueueTypeEnum.buildLbq("UnknownQueue", 2));

        Assertions.assertEquals("Cannot find specified BlockingQueue UnknownQueue", exception.getMessage());
    }

    @Test
    void testGetCodeAndNameReturnConfiguredValues() {
        Assertions.assertEquals(1, QueueTypeEnum.ARRAY_BLOCKING_QUEUE.getCode());
        Assertions.assertEquals("MemorySafeLinkedBlockingQueue", QueueTypeEnum.MEMORY_SAFE_LINKED_BLOCKING_QUEUE.getName());
    }
}
