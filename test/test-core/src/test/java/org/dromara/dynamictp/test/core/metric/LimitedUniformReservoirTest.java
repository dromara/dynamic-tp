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

package org.dromara.dynamictp.test.core.metric;

import com.codahale.metrics.Snapshot;
import org.dromara.dynamictp.core.metric.LimitedUniformReservoir;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LimitedUniformReservoir test
 *
 * @author yanhom
 * @since 1.2.2
 */
class LimitedUniformReservoirTest {

    @Test
    void testInitialState() {
        LimitedUniformReservoir reservoir = new LimitedUniformReservoir();
        assertEquals(0, reservoir.size());
    }

    @Test
    void testUpdateIncrementsSize() {
        LimitedUniformReservoir reservoir = new LimitedUniformReservoir();
        reservoir.update(100);
        assertEquals(1, reservoir.size());

        reservoir.update(200);
        assertEquals(2, reservoir.size());
    }

    @Test
    void testSizeCappedAtDefaultSize() {
        LimitedUniformReservoir reservoir = new LimitedUniformReservoir();
        for (int i = 0; i < 5000; i++) {
            reservoir.update(i);
        }
        // Should be capped at 4096
        assertEquals(4096, reservoir.size());
    }

    @Test
    void testGetSnapshot() {
        LimitedUniformReservoir reservoir = new LimitedUniformReservoir();
        reservoir.update(10);
        reservoir.update(20);
        reservoir.update(30);

        Snapshot snapshot = reservoir.getSnapshot();
        assertEquals(3, snapshot.size());
        assertEquals(20.0, snapshot.getMedian(), 1.0);
    }

    @Test
    void testGetSnapshotPercentiles() {
        LimitedUniformReservoir reservoir = new LimitedUniformReservoir();
        for (int i = 1; i <= 100; i++) {
            reservoir.update(i);
        }

        Snapshot snapshot = reservoir.getSnapshot();
        assertEquals(100, snapshot.size());
        assertTrue(snapshot.getMedian() >= 40 && snapshot.getMedian() <= 60);
        assertTrue(snapshot.get95thPercentile() >= 90);
        assertTrue(snapshot.get99thPercentile() >= 95);
    }

    @Test
    void testReset() {
        LimitedUniformReservoir reservoir = new LimitedUniformReservoir();
        reservoir.update(100);
        reservoir.update(200);
        assertEquals(2, reservoir.size());

        reservoir.reset();
        assertEquals(0, reservoir.size());
    }

    @Test
    void testResetAndReuse() {
        LimitedUniformReservoir reservoir = new LimitedUniformReservoir();
        reservoir.update(100);
        reservoir.reset();
        reservoir.update(42);

        Snapshot snapshot = reservoir.getSnapshot();
        assertEquals(1, snapshot.size());
        assertEquals(42, snapshot.getMedian(), 0.001);
    }

    @Test
    void testConcurrentUpdates() throws InterruptedException {
        LimitedUniformReservoir reservoir = new LimitedUniformReservoir();
        int threadCount = 10;
        int updatesPerThread = 500;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < updatesPerThread; j++) {
                    reservoir.update(j);
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        // All 5000 updates done, size should be capped at 4096
        assertTrue(reservoir.size() <= 4096);
        assertTrue(reservoir.size() > 0);
    }
}
