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

import org.dromara.dynamictp.core.executor.priority.Priority;
import org.dromara.dynamictp.core.executor.priority.PriorityCallable;
import org.dromara.dynamictp.core.executor.priority.PriorityFutureTask;
import org.dromara.dynamictp.core.executor.priority.PriorityRunnable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * PriorityTaskTest related.
 */
class PriorityTaskTest {

    @Test
    void testPriorityRunnableDelegatesRunAndExposesPriority() {
        AtomicBoolean executed = new AtomicBoolean(false);

        PriorityRunnable runnable = PriorityRunnable.of(() -> executed.set(true), 9);
        runnable.run();

        Assertions.assertTrue(executed.get());
        Assertions.assertEquals(9, runnable.getPriority());
    }

    @Test
    void testPriorityCallableDelegatesCallAndExposesPriority() throws Exception {
        Callable<String> callable = PriorityCallable.of(() -> "success", 7);

        String result = callable.call();

        Assertions.assertEquals("success", result);
        Assertions.assertEquals(7, ((Priority) callable).getPriority());
    }

    @Test
    void testPriorityFutureTaskWithRunnableKeepsPriorityAndResult() throws Exception {
        AtomicBoolean executed = new AtomicBoolean(false);
        PriorityRunnable runnable = PriorityRunnable.of(() -> executed.set(true), 5);
        PriorityFutureTask<String> task = new PriorityFutureTask<>(runnable, "done");

        task.run();

        Assertions.assertTrue(executed.get());
        Assertions.assertEquals(5, task.getPriority());
        Assertions.assertEquals("done", task.get());
    }

    @Test
    void testPriorityFutureTaskWithCallableKeepsPriorityAndResult() throws Exception {
        Callable<String> callable = PriorityCallable.of(() -> "done", 3);
        PriorityFutureTask<String> task = new PriorityFutureTask<>(callable);

        task.run();

        Assertions.assertEquals(3, task.getPriority());
        Assertions.assertEquals("done", task.get());
    }
}
