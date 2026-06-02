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

package org.dromara.dynamictp.test.core.support.task.callable;

import org.dromara.dynamictp.core.support.task.Ordered;
import org.dromara.dynamictp.core.support.task.callable.OrderedCallable;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * OrderedCallableTest related.
 */
class OrderedCallableTest {

    @Test
    void testOrderedCallableIsCallableAndOrdered() throws Exception {
        OrderedCallable<String> callable = new TestOrderedCallable("user:1", "done");

        assertInstanceOf(Callable.class, callable);
        assertInstanceOf(Ordered.class, callable);
        assertEquals("user:1", callable.hashKey());
        assertEquals("done", callable.call());
    }

    private static class TestOrderedCallable implements OrderedCallable<String> {

        private final Object hashKey;

        private final String result;

        TestOrderedCallable(Object hashKey, String result) {
            this.hashKey = hashKey;
            this.result = result;
        }

        @Override
        public Object hashKey() {
            return hashKey;
        }

        @Override
        public String call() {
            return result;
        }
    }
}
