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

import org.dromara.dynamictp.core.monitor.PerformanceProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PerformanceProvider test
 *
 * @author yanhom
 * @since 1.2.2
 */
class PerformanceProviderTest {

    @Test
    void testEmptySnapshot() {
        PerformanceProvider provider = new PerformanceProvider();
        PerformanceProvider.PerformanceSnapshot snapshot = provider.getSnapshotAndReset();

        assertEquals(0.0, snapshot.getAvg(), 0.001);
        assertEquals(0, snapshot.getMaxRt());
        assertEquals(0, snapshot.getMinRt());
    }

    @Test
    void testCompleteTaskUpdatesMetrics() {
        PerformanceProvider provider = new PerformanceProvider();

        provider.completeTask(100);
        provider.completeTask(200);
        provider.completeTask(300);

        PerformanceProvider.PerformanceSnapshot snapshot = provider.getSnapshotAndReset();

        assertEquals(300, snapshot.getMaxRt());
        assertEquals(100, snapshot.getMinRt());
        assertEquals(200.0, snapshot.getAvg(), 0.001);
    }

    @Test
    void testTpsCalculation() throws InterruptedException {
        PerformanceProvider provider = new PerformanceProvider();

        // wait a bit to ensure non-zero interval
        Thread.sleep(1100);

        provider.completeTask(50);
        provider.completeTask(50);
        provider.completeTask(50);

        PerformanceProvider.PerformanceSnapshot snapshot = provider.getSnapshotAndReset();

        // 3 tasks over ~1 second interval, tps should be around 3
        assertTrue(snapshot.getTps() >= 1.0, "TPS should be >= 1, was: " + snapshot.getTps());
        assertTrue(snapshot.getTps() <= 5.0, "TPS should be <= 5, was: " + snapshot.getTps());
    }

    @Test
    void testResetAfterSnapshot() throws InterruptedException {
        PerformanceProvider provider = new PerformanceProvider();

        provider.completeTask(100);
        provider.completeTask(200);

        PerformanceProvider.PerformanceSnapshot first = provider.getSnapshotAndReset();
        assertEquals(200, first.getMaxRt());

        // After reset, new snapshot should be empty
        Thread.sleep(100);
        PerformanceProvider.PerformanceSnapshot second = provider.getSnapshotAndReset();
        assertEquals(0, second.getMaxRt());
        assertEquals(0.0, second.getAvg(), 0.001);
    }

    @Test
    void testPercentiles() {
        PerformanceProvider provider = new PerformanceProvider();

        // Add enough samples for meaningful percentiles
        for (int i = 1; i <= 100; i++) {
            provider.completeTask(i);
        }

        PerformanceProvider.PerformanceSnapshot snapshot = provider.getSnapshotAndReset();

        assertEquals(100, snapshot.getMaxRt());
        assertEquals(1, snapshot.getMinRt());
        assertTrue(snapshot.getTp50() >= 40 && snapshot.getTp50() <= 60,
                "tp50 should be around 50, was: " + snapshot.getTp50());
        assertTrue(snapshot.getTp90() >= 80 && snapshot.getTp90() <= 100,
                "tp90 should be around 90, was: " + snapshot.getTp90());
        assertTrue(snapshot.getTp99() >= 95,
                "tp99 should be >= 95, was: " + snapshot.getTp99());
    }

    @Test
    void testSingleTaskSnapshot() {
        PerformanceProvider provider = new PerformanceProvider();
        provider.completeTask(42);

        PerformanceProvider.PerformanceSnapshot snapshot = provider.getSnapshotAndReset();

        assertEquals(42, snapshot.getMaxRt());
        assertEquals(42, snapshot.getMinRt());
        assertEquals(42.0, snapshot.getAvg(), 0.001);
    }
}
