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

import org.dromara.dynamictp.core.support.task.Ordered;
import org.dromara.dynamictp.core.support.task.runnable.OrderedRunnable;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OrderedRunnableTest related.
 */
class OrderedRunnableTest {

    @Test
    void testOrderedRunnableIsRunnableAndOrdered() {
        TestOrderedRunnable runnable = new TestOrderedRunnable("order-key");

        assertInstanceOf(Runnable.class, runnable);
        assertInstanceOf(Ordered.class, runnable);
        assertEquals("order-key", runnable.hashKey());

        runnable.run();

        assertTrue(runnable.executed.get());
    }

    private static class TestOrderedRunnable implements OrderedRunnable {

        private final Object hashKey;

        private final AtomicBoolean executed = new AtomicBoolean(false);

        TestOrderedRunnable(Object hashKey) {
            this.hashKey = hashKey;
        }

        @Override
        public Object hashKey() {
            return hashKey;
        }

        @Override
        public void run() {
            executed.set(true);
        }
    }
}
