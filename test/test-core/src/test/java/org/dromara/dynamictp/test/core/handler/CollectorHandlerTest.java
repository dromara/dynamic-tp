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

package org.dromara.dynamictp.test.core.handler;

import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.core.handler.CollectorHandler;
import org.dromara.dynamictp.core.monitor.collector.MetricsCollector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * CollectorHandler test.
 *
 * @author codex
 */
@Execution(ExecutionMode.SAME_THREAD)
class CollectorHandlerTest {

    private static final String TEST_TYPE = "test_collector";

    private Map<String, MetricsCollector> collectors;

    @BeforeEach
    void setUp() throws Exception {
        collectors = collectors();
    }

    @AfterEach
    void tearDown() {
        collectors.remove(TEST_TYPE);
    }

    @Test
    void testCollectDispatchesSupportedTypesIgnoringCase() {
        RecordingCollector collector = new RecordingCollector();
        ThreadPoolStats stats = new ThreadPoolStats();
        collectors.put(TEST_TYPE, collector);

        CollectorHandler.getInstance().collect(stats, Collections.singletonList(TEST_TYPE.toUpperCase()));

        assertSame(stats, collector.collected.get());
    }

    @Test
    void testCollectSkipsNullStatsEmptyTypesAndUnknownTypes() {
        RecordingCollector collector = new RecordingCollector();
        ThreadPoolStats stats = new ThreadPoolStats();
        collectors.put(TEST_TYPE, collector);

        CollectorHandler handler = CollectorHandler.getInstance();
        handler.collect(null, Collections.singletonList(TEST_TYPE));
        handler.collect(stats, Collections.emptyList());
        handler.collect(stats, Arrays.asList("unknown"));

        assertNull(collector.collected.get());
    }

    @SuppressWarnings("unchecked")
    private Map<String, MetricsCollector> collectors() throws Exception {
        Field field = CollectorHandler.class.getDeclaredField("COLLECTORS");
        field.setAccessible(true);
        return (Map<String, MetricsCollector>) field.get(null);
    }

    private static class RecordingCollector implements MetricsCollector {

        private final AtomicReference<ThreadPoolStats> collected = new AtomicReference<>();

        @Override
        public void collect(ThreadPoolStats poolStats) {
            collected.set(poolStats);
        }

        @Override
        public String type() {
            return TEST_TYPE;
        }

        @Override
        public boolean support(String type) {
            return TEST_TYPE.equalsIgnoreCase(type);
        }
    }
}
