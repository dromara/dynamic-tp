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

package org.dromara.dynamictp.test.core.executor;

import org.dromara.dynamictp.core.executor.NamedThreadFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NamedThreadFactoryTest related
 *
 * @author Copilot
 */
class NamedThreadFactoryTest {

    @Test
    void testNewThreadUsesPrefixDaemonAndPriority() {
        NamedThreadFactory factory = new NamedThreadFactory("worker", true, Thread.MAX_PRIORITY);

        Thread first = factory.newThread(() -> { });
        Thread second = factory.newThread(() -> { });

        assertEquals("worker-1", first.getName());
        assertEquals("worker-2", second.getName());
        assertTrue(first.isDaemon());
        assertEquals(Thread.MAX_PRIORITY, first.getPriority());
        assertSame(Thread.currentThread().getThreadGroup(), first.getThreadGroup());
    }

    @Test
    void testSetNamePrefixAffectsSubsequentThreads() {
        NamedThreadFactory factory = new NamedThreadFactory("worker");

        factory.setNamePrefix("biz");
        Thread thread = factory.newThread(() -> { });

        assertEquals("biz", factory.getNamePrefix());
        assertEquals("biz-1", thread.getName());
        assertFalse(thread.isDaemon());
        assertEquals(Thread.NORM_PRIORITY, thread.getPriority());
    }
}
