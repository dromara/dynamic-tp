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

package org.dromara.dynamictp.test.core.thread;

import org.dromara.dynamictp.core.executor.eager.EagerDtpExecutor;
import org.dromara.dynamictp.core.executor.eager.TaskQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TaskQueueTest related.
 */
class TaskQueueTest {

    @Test
    void testOfferRejectsWhenExecutorMissing() {
        TaskQueue queue = new TaskQueue(1);

        Assertions.assertThrows(RejectedExecutionException.class, () -> queue.offer(() -> { }));
    }

    @Test
    void testOfferQueuesWhenPoolHasReachedMaximum() {
        TaskQueue queue = new TaskQueue(1);
        EagerDtpExecutor executor = mock(EagerDtpExecutor.class);
        when(executor.getPoolSize()).thenReturn(2);
        when(executor.getMaximumPoolSize()).thenReturn(2);
        queue.setExecutor(executor);

        Assertions.assertTrue(queue.offer(() -> { }));
        Assertions.assertEquals(1, queue.size());
    }

    @Test
    void testOfferQueuesWhenSubmittedTaskCountDoesNotExceedPoolSize() {
        TaskQueue queue = new TaskQueue(1);
        EagerDtpExecutor executor = mock(EagerDtpExecutor.class);
        when(executor.getPoolSize()).thenReturn(1);
        when(executor.getMaximumPoolSize()).thenReturn(2);
        when(executor.getSubmittedTaskCount()).thenReturn(1);
        queue.setExecutor(executor);

        Assertions.assertTrue(queue.offer(() -> { }));
        Assertions.assertEquals(1, queue.size());
    }

    @Test
    void testOfferReturnsFalseToTriggerNewWorkerWhenPoolCanGrow() {
        TaskQueue queue = new TaskQueue(1);
        EagerDtpExecutor executor = mock(EagerDtpExecutor.class);
        when(executor.getPoolSize()).thenReturn(1);
        when(executor.getMaximumPoolSize()).thenReturn(2);
        when(executor.getSubmittedTaskCount()).thenReturn(2);
        queue.setExecutor(executor);

        Assertions.assertFalse(queue.offer(() -> { }));
        Assertions.assertEquals(0, queue.size());
    }

    @Test
    void testForceRejectsWhenExecutorShutdown() {
        TaskQueue queue = new TaskQueue(1);
        EagerDtpExecutor executor = mock(EagerDtpExecutor.class);
        when(executor.isShutdown()).thenReturn(true);
        queue.setExecutor(executor);

        Assertions.assertThrows(RejectedExecutionException.class, () -> queue.force(() -> { }, 0, TimeUnit.MILLISECONDS));
    }

    @Test
    void testForceOffersWhenExecutorRunning() throws InterruptedException {
        TaskQueue queue = new TaskQueue(1);
        EagerDtpExecutor executor = mock(EagerDtpExecutor.class);
        when(executor.isShutdown()).thenReturn(false);
        queue.setExecutor(executor);

        Assertions.assertTrue(queue.force(() -> { }, 0, TimeUnit.MILLISECONDS));
        Assertions.assertEquals(1, queue.size());
    }
}
