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
import org.dromara.dynamictp.core.support.ThreadPoolCreator;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ThreadPoolCreator test
 *
 * @author yanhom
 * @since 1.2.2
 */
class ThreadPoolCreatorTest {

    @Test
    void testCreateCommonFast() {
        ThreadPoolExecutor executor = ThreadPoolCreator.createCommonFast("common-fast");
        try {
            assertEquals(1, executor.getCorePoolSize());
            assertEquals(Runtime.getRuntime().availableProcessors(), executor.getMaximumPoolSize());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void testCreateDynamicFast() {
        DtpExecutor executor = ThreadPoolCreator.createDynamicFast("dynamic-fast");
        try {
            assertEquals("dynamic-fast", executor.getThreadPoolName());
            assertEquals(1, executor.getCorePoolSize());
            assertEquals(Runtime.getRuntime().availableProcessors(), executor.getMaximumPoolSize());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void testCreateDynamicFastWithPrefix() {
        DtpExecutor executor = ThreadPoolCreator.createDynamicFast("dynamic-prefix", "my-prefix");
        try {
            assertEquals("dynamic-prefix", executor.getThreadPoolName());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void testNewSingleThreadPool() {
        ThreadPoolExecutor executor = ThreadPoolCreator.newSingleThreadPool("single", 512);
        try {
            assertEquals(1, executor.getCorePoolSize());
            assertEquals(1, executor.getMaximumPoolSize());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void testNewFixedThreadPool() {
        ThreadPoolExecutor executor = ThreadPoolCreator.newFixedThreadPool("fixed", 4, 1024);
        try {
            assertEquals(4, executor.getCorePoolSize());
            assertEquals(4, executor.getMaximumPoolSize());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void testNewThreadPool() {
        ThreadPoolExecutor executor = ThreadPoolCreator.newThreadPool("pool", 2, 8, 256);
        try {
            assertEquals(2, executor.getCorePoolSize());
            assertEquals(8, executor.getMaximumPoolSize());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void testNewScheduledThreadPool() {
        ScheduledExecutorService executor = ThreadPoolCreator.newScheduledThreadPool("scheduled", 2);
        try {
            assertTrue(executor instanceof ScheduledExecutorService);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void testNewExecutorByBlockingCoefficient() {
        ThreadPoolExecutor executor = ThreadPoolCreator.newExecutorByBlockingCoefficient(0.5f);
        try {
            int expectedPoolSize = (int) (Runtime.getRuntime().availableProcessors() / (1 - 0.5f));
            assertEquals(expectedPoolSize, executor.getCorePoolSize());
            assertEquals(expectedPoolSize, executor.getMaximumPoolSize());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void testNewExecutorByBlockingCoefficientThrowsForOne() {
        assertThrows(IllegalArgumentException.class,
                () -> ThreadPoolCreator.newExecutorByBlockingCoefficient(1.0f));
    }

    @Test
    void testNewExecutorByBlockingCoefficientThrowsForNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> ThreadPoolCreator.newExecutorByBlockingCoefficient(-0.5f));
    }
}
