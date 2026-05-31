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

package org.dromara.dynamictp.test.core.selector;

import org.dromara.dynamictp.core.support.selector.HashedExecutorSelector;
import org.dromara.dynamictp.core.support.selector.RandomExecutorSelector;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ExecutorSelector test
 *
 * @author yanhom
 * @since 1.2.2
 */
class ExecutorSelectorTest {

    @Test
    void testHashedSelectConsistent() {
        HashedExecutorSelector selector = new HashedExecutorSelector();
        ExecutorService e1 = Executors.newSingleThreadExecutor();
        ExecutorService e2 = Executors.newSingleThreadExecutor();
        ExecutorService e3 = Executors.newSingleThreadExecutor();
        List<Executor> executors = Arrays.asList(e1, e2, e3);

        // Same arg should always select the same executor
        String key = "test-key";
        Executor first = selector.select(executors, key);
        Executor second = selector.select(executors, key);
        assertEquals(first, second);

        // cleanup
        e1.shutdownNow();
        e2.shutdownNow();
        e3.shutdownNow();
    }

    @Test
    void testHashedSelectDifferentKeys() {
        HashedExecutorSelector selector = new HashedExecutorSelector();
        ExecutorService e1 = Executors.newSingleThreadExecutor();
        ExecutorService e2 = Executors.newSingleThreadExecutor();
        List<Executor> executors = Arrays.asList(e1, e2);

        // Different keys should still return valid executors
        Executor r1 = selector.select(executors, "key-a");
        Executor r2 = selector.select(executors, "key-b");
        assertNotNull(r1);
        assertNotNull(r2);
        assertTrue(executors.contains(r1));
        assertTrue(executors.contains(r2));

        e1.shutdownNow();
        e2.shutdownNow();
    }

    @Test
    void testHashedNegativeHashCode() {
        HashedExecutorSelector selector = new HashedExecutorSelector();
        ExecutorService e1 = Executors.newSingleThreadExecutor();
        ExecutorService e2 = Executors.newSingleThreadExecutor();
        List<Executor> executors = Arrays.asList(e1, e2);

        // Object with negative hashCode
        Object negativeKey = new Object() {
            @Override
            public int hashCode() {
                return -1;
            }
        };
        Executor result = selector.select(executors, negativeKey);
        assertNotNull(result);
        assertTrue(executors.contains(result));

        e1.shutdownNow();
        e2.shutdownNow();
    }

    @Test
    void testRandomSelectReturnsValid() {
        RandomExecutorSelector selector = new RandomExecutorSelector();
        ExecutorService e1 = Executors.newSingleThreadExecutor();
        ExecutorService e2 = Executors.newSingleThreadExecutor();
        ExecutorService e3 = Executors.newSingleThreadExecutor();
        List<Executor> executors = Arrays.asList(e1, e2, e3);

        // Run multiple times to verify it always returns a valid executor
        for (int i = 0; i < 100; i++) {
            Executor selected = selector.select(executors, "any");
            assertNotNull(selected);
            assertTrue(executors.contains(selected));
        }

        e1.shutdownNow();
        e2.shutdownNow();
        e3.shutdownNow();
    }
}
