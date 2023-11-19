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

package org.dromara.dynamictp.core.monitor;

import lombok.Getter;
import lombok.val;
import org.dromara.dynamictp.core.metric.MMAPCounter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PerformanceProvider related
 *
 * @author kyao
 * @since 1.1.5
 */
public class PerformanceProvider {

    /**
     * last refresh timestamp
     */
    private final AtomicLong lastRefreshMillis = new AtomicLong(System.currentTimeMillis());

    private final MMAPCounter mmapCounter = new MMAPCounter();

    public void completeTask(long rt) {
        mmapCounter.add(rt);
    }

    public PerformanceSnapshot getSnapshotAndReset() {
        long currentMillis = System.currentTimeMillis();
        int intervalTs = (int) (currentMillis - lastRefreshMillis.get()) / 1000;
        val performanceSnapshot = new PerformanceSnapshot(mmapCounter, intervalTs);
        reset(currentMillis);
        return performanceSnapshot;
    }

    private void reset(long currentMillis) {
        mmapCounter.reset();
        lastRefreshMillis.compareAndSet(lastRefreshMillis.get(), currentMillis);
    }

    @Getter
    public static class PerformanceSnapshot {

        private final double tps;

        private final long maxRt;

        private final long minRt;

        private final double avg;

        private final double tp50;

        private final double tp75;

        private final double tp90;

        private final double tp95;

        private final double tp99;

        private final double tp999;

        public PerformanceSnapshot(MMAPCounter mmapCounter, int monitorInterval) {
            tps = BigDecimal.valueOf(mmapCounter.getMmaCounter().getCount())
                    .divide(BigDecimal.valueOf(Math.max(monitorInterval, 1)), 1, RoundingMode.HALF_UP)
                    .doubleValue();

            maxRt = mmapCounter.getMmaCounter().getMax();
            minRt = mmapCounter.getMmaCounter().getMin();
            avg = mmapCounter.getMmaCounter().getAvg();

            tp50 = mmapCounter.getSnapshot().getMedian();
            tp75 = mmapCounter.getSnapshot().get75thPercentile();
            tp90 = mmapCounter.getSnapshot().getValue(0.9);
            tp95 = mmapCounter.getSnapshot().get95thPercentile();
            tp99 = mmapCounter.getSnapshot().get99thPercentile();
            tp999 = mmapCounter.getSnapshot().get999thPercentile();
        }
    }
}
