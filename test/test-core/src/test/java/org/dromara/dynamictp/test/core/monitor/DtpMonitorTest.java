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

package org.dromara.dynamictp.test.core.monitor;

import org.dromara.dynamictp.common.event.CustomContextRefreshedEvent;
import org.dromara.dynamictp.common.manager.EventBusManager;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.monitor.DtpMonitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DtpMonitor test.
 *
 * @author codex
 */
@Execution(ExecutionMode.SAME_THREAD)
class DtpMonitorTest {

    private DtpProperties properties;

    private int originalInterval;

    private Object originalExecutor;

    private DtpMonitor monitor;

    @BeforeEach
    void setUp() throws Exception {
        properties = DtpProperties.getInstance();
        originalInterval = properties.getMonitorInterval();
        originalExecutor = getStaticField("monitorExecutor");
    }

    @AfterEach
    void tearDown() throws Exception {
        properties.setMonitorInterval(originalInterval);
        if (monitor != null) {
            EventBusManager.unregister(monitor);
        }
        Object executor = getStaticField("monitorExecutor");
        if (executor instanceof RecordingScheduledExecutor) {
            ((RecordingScheduledExecutor) executor).shutdownNow();
        }
        setStaticField("monitorExecutor", originalExecutor);
    }

    @Test
    void testContextRefreshSchedulesMonitorAndCancelsPreviousTask() throws Exception {
        RecordingScheduledExecutor executor = new RecordingScheduledExecutor();
        setStaticField("monitorExecutor", executor);
        monitor = new DtpMonitor(properties);
        CustomContextRefreshedEvent event = new CustomContextRefreshedEvent(this);

        properties.setMonitorInterval(5);
        monitor.onContextRefreshedEvent(event);
        properties.setMonitorInterval(10);
        monitor.onContextRefreshedEvent(event);

        assertEquals(2, executor.delays.size());
        assertEquals(Long.valueOf(5), executor.delays.get(0));
        assertEquals(Long.valueOf(10), executor.delays.get(1));
        assertTrue(executor.futures.get(0).cancelled);
    }

    @Test
    void testContextRefreshDoesNothingWhenIntervalIsUnchanged() throws Exception {
        RecordingScheduledExecutor executor = new RecordingScheduledExecutor();
        setStaticField("monitorExecutor", executor);
        monitor = new DtpMonitor(properties);
        properties.setMonitorInterval(5);
        setField(monitor, "monitorInterval", 5);

        monitor.onContextRefreshedEvent(new CustomContextRefreshedEvent(this));

        assertTrue(executor.delays.isEmpty());
    }

    @Test
    void testDestroyShutsDownMonitorExecutor() throws Exception {
        RecordingScheduledExecutor executor = new RecordingScheduledExecutor();
        setStaticField("monitorExecutor", executor);

        DtpMonitor.destroy();

        assertTrue(executor.isShutdown());
    }

    private void setStaticField(String name, Object value) throws Exception {
        Field field = DtpMonitor.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }

    private Object getStaticField(String name) throws Exception {
        Field field = DtpMonitor.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static class RecordingScheduledExecutor extends ScheduledThreadPoolExecutor {

        private final List<Long> delays = new ArrayList<>();

        private final List<RecordingScheduledFuture> futures = new ArrayList<>();

        RecordingScheduledExecutor() {
            super(1);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(
                Runnable command, long initialDelay, long delay, TimeUnit unit) {
            delays.add(unit.toSeconds(delay));
            RecordingScheduledFuture future = new RecordingScheduledFuture();
            futures.add(future);
            return future;
        }
    }

    private static class RecordingScheduledFuture implements ScheduledFuture<Object> {

        private boolean cancelled;

        @Override
        public long getDelay(TimeUnit unit) {
            return 0;
        }

        @Override
        public int compareTo(Delayed other) {
            return 0;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            cancelled = true;
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return cancelled;
        }

        @Override
        public Object get() {
            return null;
        }

        @Override
        public Object get(long timeout, TimeUnit unit) {
            return null;
        }
    }
}
