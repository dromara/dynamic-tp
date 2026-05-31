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

package org.dromara.dynamictp.test.core;

import org.dromara.dynamictp.common.ex.DtpException;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolBuilder;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DtpRegistry basic operations test.
 * Refresh logic is tightly coupled with Spring context and is covered
 * by integration tests in {@code AbstractRefresherTest}.
 *
 * @author yanhom
 * @since 1.2.2
 */
class DtpRegistryRefreshTest {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private static String uniqueName(String prefix) {
        return prefix + "-" + COUNTER.incrementAndGet();
    }

    @Test
    void testRegisterAndGetExecutor() {
        String name = uniqueName("register-get");
        DtpExecutor executor = ThreadPoolBuilder.newBuilder()
                .threadPoolName(name)
                .buildDynamic();
        try {
            DtpRegistry.registerExecutor(ExecutorWrapper.of(executor), "test");

            assertNotNull(DtpRegistry.getExecutor(name));
            assertEquals(name, ((DtpExecutor) DtpRegistry.getExecutor(name)).getThreadPoolName());
        } finally {
            DtpRegistry.unregisterExecutor(name);
            executor.shutdownNow();
        }
    }

    @Test
    void testGetDtpExecutor() {
        String name = uniqueName("get-dtp");
        DtpExecutor executor = ThreadPoolBuilder.newBuilder()
                .threadPoolName(name)
                .buildDynamic();
        try {
            DtpRegistry.registerExecutor(ExecutorWrapper.of(executor), "test");

            DtpExecutor found = DtpRegistry.getDtpExecutor(name);
            assertEquals(name, found.getThreadPoolName());
        } finally {
            DtpRegistry.unregisterExecutor(name);
            executor.shutdownNow();
        }
    }

    @Test
    void testGetExecutorNotFoundThrows() {
        assertThrows(DtpException.class, () -> DtpRegistry.getExecutor("nonexistent-" + System.nanoTime()));
    }

    @Test
    void testGetDtpExecutorNotFoundThrows() {
        assertThrows(DtpException.class, () -> DtpRegistry.getDtpExecutor("nonexistent-" + System.nanoTime()));
    }

    @Test
    void testUnregisterExecutor() {
        String name = uniqueName("unregister");
        DtpExecutor executor = ThreadPoolBuilder.newBuilder()
                .threadPoolName(name)
                .buildDynamic();
        try {
            DtpRegistry.registerExecutor(ExecutorWrapper.of(executor), "test");
            assertNotNull(DtpRegistry.getExecutor(name));

            DtpRegistry.unregisterExecutor(name);

            assertThrows(DtpException.class, () -> DtpRegistry.getExecutor(name));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void testGetAllExecutorNames() {
        String name = uniqueName("all-names");
        DtpExecutor executor = ThreadPoolBuilder.newBuilder()
                .threadPoolName(name)
                .buildDynamic();
        try {
            DtpRegistry.registerExecutor(ExecutorWrapper.of(executor), "test");
            assertTrue(DtpRegistry.getAllExecutorNames().contains(name));
        } finally {
            DtpRegistry.unregisterExecutor(name);
            executor.shutdownNow();
        }
    }

    @Test
    void testGetAllExecutors() {
        String name = uniqueName("all-executors");
        DtpExecutor executor = ThreadPoolBuilder.newBuilder()
                .threadPoolName(name)
                .buildDynamic();
        try {
            DtpRegistry.registerExecutor(ExecutorWrapper.of(executor), "test");
            assertTrue(DtpRegistry.getAllExecutors().containsKey(name));
        } finally {
            DtpRegistry.unregisterExecutor(name);
            executor.shutdownNow();
        }
    }

    @Test
    void testRegisterDuplicateIsIdempotent() {
        String name = uniqueName("dup-register");
        DtpExecutor executor = ThreadPoolBuilder.newBuilder()
                .threadPoolName(name)
                .buildDynamic();
        try {
            DtpRegistry.registerExecutor(ExecutorWrapper.of(executor), "test");
            DtpRegistry.registerExecutor(ExecutorWrapper.of(executor), "test2");

            // still the first one (putIfAbsent)
            assertNotNull(DtpRegistry.getExecutor(name));
        } finally {
            DtpRegistry.unregisterExecutor(name);
            executor.shutdownNow();
        }
    }

    @Test
    void testGetExecutorWrapper() {
        String name = uniqueName("wrapper");
        DtpExecutor executor = ThreadPoolBuilder.newBuilder()
                .threadPoolName(name)
                .buildDynamic();
        try {
            DtpRegistry.registerExecutor(ExecutorWrapper.of(executor), "test");

            ExecutorWrapper wrapper = DtpRegistry.getExecutorWrapper(name);
            assertNotNull(wrapper);
            assertEquals(name, wrapper.getThreadPoolName());
        } finally {
            DtpRegistry.unregisterExecutor(name);
            executor.shutdownNow();
        }
    }
}
