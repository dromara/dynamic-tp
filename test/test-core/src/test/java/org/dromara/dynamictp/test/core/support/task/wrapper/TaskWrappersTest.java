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

package org.dromara.dynamictp.test.core.support.task.wrapper;

import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrappers;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TaskWrappers test
 *
 * @author yanhom
 * @since 1.2.2
 */
class TaskWrappersTest {

    @Test
    void testGetInstanceNotNull() {
        TaskWrappers instance = TaskWrappers.getInstance();
        assertNotNull(instance);
    }

    @Test
    void testGetInstanceSingleton() {
        TaskWrappers a = TaskWrappers.getInstance();
        TaskWrappers b = TaskWrappers.getInstance();
        assertTrue(a == b);
    }

    @Test
    void testGetByNamesTtl() {
        TaskWrappers instance = TaskWrappers.getInstance();
        Set<String> names = new HashSet<>();
        names.add("ttl");

        List<TaskWrapper> wrappers = instance.getByNames(names);
        assertEquals(1, wrappers.size());
        assertEquals("ttl", wrappers.get(0).name());
    }

    @Test
    void testGetByNamesMdc() {
        TaskWrappers instance = TaskWrappers.getInstance();
        Set<String> names = new HashSet<>();
        names.add("mdc");

        List<TaskWrapper> wrappers = instance.getByNames(names);
        assertEquals(1, wrappers.size());
        assertEquals("mdc", wrappers.get(0).name());
    }

    @Test
    void testGetByNamesBoth() {
        TaskWrappers instance = TaskWrappers.getInstance();
        Set<String> names = new HashSet<>();
        names.add("ttl");
        names.add("mdc");

        List<TaskWrapper> wrappers = instance.getByNames(names);
        assertEquals(2, wrappers.size());
    }

    @Test
    void testGetByNamesEmptyReturnsEmpty() {
        TaskWrappers instance = TaskWrappers.getInstance();
        List<TaskWrapper> wrappers = instance.getByNames(null);
        assertTrue(wrappers.isEmpty());

        wrappers = instance.getByNames(new HashSet<>());
        assertTrue(wrappers.isEmpty());
    }

    @Test
    void testGetByNamesUnknownReturnsEmpty() {
        TaskWrappers instance = TaskWrappers.getInstance();
        Set<String> names = new HashSet<>();
        names.add("nonexistent");

        List<TaskWrapper> wrappers = instance.getByNames(names);
        assertTrue(wrappers.isEmpty());
    }

    @Test
    void testRegisterCustomWrapper() {
        TaskWrappers.register(new TaskWrapper() {
            @Override
            public String name() {
                return "custom-test";
            }

            @Override
            public Runnable wrap(Runnable runnable) {
                return runnable;
            }
        });

        TaskWrappers instance = TaskWrappers.getInstance();
        Set<String> names = new HashSet<>();
        names.add("custom-test");

        List<TaskWrapper> wrappers = instance.getByNames(names);
        assertEquals(1, wrappers.size());
        assertEquals("custom-test", wrappers.get(0).name());
    }

    @Test
    void testRegisterDuplicateIgnored() {
        // Register TTL again - should be ignored since it already exists
        TaskWrappers.register(new TaskWrapper() {
            @Override
            public String name() {
                return "ttl";
            }

            @Override
            public Runnable wrap(Runnable runnable) {
                return runnable;
            }
        });

        TaskWrappers instance = TaskWrappers.getInstance();
        Set<String> names = new HashSet<>();
        names.add("ttl");

        List<TaskWrapper> wrappers = instance.getByNames(names);
        assertEquals(1, wrappers.size());
    }

    @Test
    void testTtlWrapperWraps() {
        TaskWrappers instance = TaskWrappers.getInstance();
        Set<String> names = new HashSet<>();
        names.add("ttl");

        List<TaskWrapper> wrappers = instance.getByNames(names);
        TaskWrapper ttlWrapper = wrappers.get(0);

        Runnable original = () -> { };
        Runnable wrapped = ttlWrapper.wrap(original);
        assertNotNull(wrapped);
    }

    @Test
    void testMdcWrapperWraps() {
        TaskWrappers instance = TaskWrappers.getInstance();
        Set<String> names = new HashSet<>();
        names.add("mdc");

        List<TaskWrapper> wrappers = instance.getByNames(names);
        TaskWrapper mdcWrapper = wrappers.get(0);

        Runnable original = () -> { };
        Runnable wrapped = mdcWrapper.wrap(original);
        assertNotNull(wrapped);
    }
}
