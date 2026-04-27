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

import org.dromara.dynamictp.core.executor.NamedThreadFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NamedThreadFactory test
 *
 * @author yanhom
 * @since 1.2.2
 */
class NamedThreadFactoryTest {

    @Test
    void testNewThreadWithDefaultConstructor() {
        NamedThreadFactory factory = new NamedThreadFactory("test-pool");
        Thread t = factory.newThread(() -> { });

        assertNotNull(t);
        assertEquals("test-pool-1", t.getName());
        assertFalse(t.isDaemon());
        assertEquals(Thread.NORM_PRIORITY, t.getPriority());
    }

    @Test
    void testNewThreadWithDaemon() {
        NamedThreadFactory factory = new NamedThreadFactory("daemon-pool", true);
        Thread t = factory.newThread(() -> { });

        assertTrue(t.isDaemon());
        assertEquals("daemon-pool-1", t.getName());
    }

    @Test
    void testNewThreadWithPriority() {
        NamedThreadFactory factory = new NamedThreadFactory("prio-pool", false, Thread.MAX_PRIORITY);
        Thread t = factory.newThread(() -> { });

        assertEquals(Thread.MAX_PRIORITY, t.getPriority());
        assertFalse(t.isDaemon());
    }

    @Test
    void testSequenceIncrements() {
        NamedThreadFactory factory = new NamedThreadFactory("seq-pool");

        Thread t1 = factory.newThread(() -> { });
        Thread t2 = factory.newThread(() -> { });
        Thread t3 = factory.newThread(() -> { });

        assertEquals("seq-pool-1", t1.getName());
        assertEquals("seq-pool-2", t2.getName());
        assertEquals("seq-pool-3", t3.getName());
    }

    @Test
    void testGetNamePrefix() {
        NamedThreadFactory factory = new NamedThreadFactory("my-prefix");
        assertEquals("my-prefix", factory.getNamePrefix());
    }

    @Test
    void testSetNamePrefix() {
        NamedThreadFactory factory = new NamedThreadFactory("old-prefix");
        factory.setNamePrefix("new-prefix");

        assertEquals("new-prefix", factory.getNamePrefix());

        Thread t = factory.newThread(() -> { });
        assertEquals("new-prefix-1", t.getName());
    }

    @Test
    void testThreadGroupInheritedFromCurrentThread() {
        NamedThreadFactory factory = new NamedThreadFactory("group-pool");
        Thread t = factory.newThread(() -> { });

        assertEquals(Thread.currentThread().getThreadGroup(), t.getThreadGroup());
    }
}
