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
 * TpPerformanceProvider related
 *
 * @author kyao
 * @since 1.1.5
 */
public class TpPerformanceProvider {

    private final AtomicLong intervalCounter = new AtomicLong();

    /**
     * last refresh time
     */
    private long lastRefreshTime = this.getCurrentSeconds();

    private final MMAPCounter mmapCounter = new MMAPCounter();

    public void finishTask(long rt) {
        intervalCounter.incrementAndGet();
        mmapCounter.add(rt);
    }

    public PerformanceSnapshot getSnapshotAndReset() {
        long currentSeconds = getCurrentSeconds();
        int monitorInterval = (int) (currentSeconds - lastRefreshTime);
        val performanceSnapshot = new PerformanceSnapshot(mmapCounter, intervalCounter.get(), monitorInterval);
        reset(currentSeconds);
        return performanceSnapshot;
    }

    private void reset(long currentSeconds) {
        mmapCounter.reset();
        intervalCounter.set(0);
        lastRefreshTime = currentSeconds;
    }

    private long getCurrentSeconds() {
        return System.currentTimeMillis() / 1000;
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

        public PerformanceSnapshot(MMAPCounter mmapCounter, long intervalCounter, int interval) {
            tps = BigDecimal.valueOf(intervalCounter).divide(BigDecimal.valueOf(Math.max(interval, 1)),
                    1, RoundingMode.HALF_UP).doubleValue();

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
