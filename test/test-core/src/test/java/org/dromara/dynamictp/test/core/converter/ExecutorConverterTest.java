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

package org.dromara.dynamictp.test.core.converter;

import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.common.entity.TpMainFields;
import org.dromara.dynamictp.core.converter.ExecutorConverter;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ExecutorConverter test
 *
 * @author yanhom
 * @since 1.2.2
 */
class ExecutorConverterTest {

    private DtpExecutor dtpExecutor;

    @AfterEach
    void tearDown() {
        if (dtpExecutor != null) {
            dtpExecutor.shutdownNow();
        }
    }

    @Test
    void testToMainFields() {
        dtpExecutor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("converter-main")
                .corePoolSize(4)
                .maximumPoolSize(8)
                .keepAliveTime(60)
                .timeUnit(TimeUnit.SECONDS)
                .buildDynamic();

        ExecutorWrapper wrapper = ExecutorWrapper.of(dtpExecutor);
        TpMainFields fields = ExecutorConverter.toMainFields(wrapper);

        assertEquals("converter-main", fields.getThreadPoolName());
        assertEquals(4, fields.getCorePoolSize());
        assertEquals(8, fields.getMaxPoolSize());
        assertEquals(60, fields.getKeepAliveTime());
        assertNotNull(fields.getQueueType());
        assertNotNull(fields.getRejectType());
        assertFalse(fields.isAllowCoreThreadTimeOut());
    }

    @Test
    void testToMainFieldsWithAllowCoreThreadTimeOut() {
        dtpExecutor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("converter-timeout")
                .allowCoreThreadTimeOut(true)
                .buildDynamic();

        ExecutorWrapper wrapper = ExecutorWrapper.of(dtpExecutor);
        TpMainFields fields = ExecutorConverter.toMainFields(wrapper);

        assertTrue(fields.isAllowCoreThreadTimeOut());
    }

    @Test
    void testToMetrics() {
        dtpExecutor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("converter-metrics")
                .corePoolSize(2)
                .maximumPoolSize(4)
                .keepAliveTime(30)
                .timeUnit(TimeUnit.SECONDS)
                .buildDynamic();

        ExecutorWrapper wrapper = ExecutorWrapper.of(dtpExecutor);
        ThreadPoolStats stats = ExecutorConverter.toMetrics(wrapper);

        assertNotNull(stats);
        assertEquals("converter-metrics", stats.getPoolName());
        assertEquals(2, stats.getCorePoolSize());
        assertEquals(4, stats.getMaximumPoolSize());
        assertTrue(stats.isDynamic());
        assertNotNull(stats.getQueueType());
    }

    @Test
    void testToMetricsContainsPerformanceData() {
        dtpExecutor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("converter-perf")
                .buildDynamic();

        // Simulate a task completion through stat provider
        ExecutorWrapper wrapper = ExecutorWrapper.of(dtpExecutor);
        Runnable task = () -> { };
        wrapper.getThreadPoolStatProvider().startTask(task);
        wrapper.getThreadPoolStatProvider().completeTask(task);

        ThreadPoolStats stats = ExecutorConverter.toMetrics(wrapper);

        assertNotNull(stats);
        assertEquals("converter-perf", stats.getPoolName());
    }

    @Test
    void testToMetricsPoolSize() {
        dtpExecutor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("converter-size")
                .corePoolSize(3)
                .maximumPoolSize(6)
                .buildDynamic();

        ExecutorWrapper wrapper = ExecutorWrapper.of(dtpExecutor);
        ThreadPoolStats stats = ExecutorConverter.toMetrics(wrapper);

        assertNotNull(stats);
        assertEquals(3, stats.getCorePoolSize());
        assertEquals(6, stats.getMaximumPoolSize());
        assertEquals(0, stats.getActiveCount());
        assertEquals(0, stats.getPoolSize());
    }
}
