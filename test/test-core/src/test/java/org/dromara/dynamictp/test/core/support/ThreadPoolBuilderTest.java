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

package org.dromara.dynamictp.test.core.support;

import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.support.ThreadPoolBuilder;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
public class ThreadPoolBuilderTest {

    @Test
    void testBuildDynamicThrowsWhenBothPriorityAndOrderedSet() {
        assertThrows(IllegalArgumentException.class, () -> ThreadPoolBuilder.newBuilder()
                .threadPoolName("dtpExecutor1")
                .priority()
                .ordered()
                .buildDynamic());
    }

    @Test
    void testBuildDynamic() {
        DtpExecutor executor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("testDynamic")
                .corePoolSize(4)
                .maximumPoolSize(8)
                .keepAliveTime(30)
                .timeUnit(TimeUnit.SECONDS)
                .buildDynamic();

        assertNotNull(executor);
        assertEquals("testDynamic", executor.getThreadPoolName());
        assertEquals(4, executor.getCorePoolSize());
        assertEquals(8, executor.getMaximumPoolSize());
        executor.shutdown();
    }

    @Test
    void testBuildCommon() {
        ThreadPoolExecutor executor = ThreadPoolBuilder.newBuilder()
                .corePoolSize(2)
                .maximumPoolSize(4)
                .keepAliveTime(60)
                .timeUnit(TimeUnit.SECONDS)
                .buildCommon();

        assertNotNull(executor);
        assertInstanceOf(ThreadPoolExecutor.class, executor);
        assertEquals(2, executor.getCorePoolSize());
        assertEquals(4, executor.getMaximumPoolSize());
        executor.shutdown();
    }

    @Test
    void testBuildPriority() {
        assertNotNull(ThreadPoolBuilder.newBuilder()
                .threadPoolName("priorityPool")
                .corePoolSize(2)
                .maximumPoolSize(4)
                .buildPriority());
    }

}
