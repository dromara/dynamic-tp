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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TpPerformanceProvider related
 *
 * @author kyao
 * @since 1.1.5
 */
public class TpPerformanceProvider {

    /**
     * 任务执行耗时列表
     */
    private final CopyOnWriteArrayList<Long> rtList = new CopyOnWriteArrayList<>();

    /**
     * 上一次刷新数据时间
     */
    private long lastRefreshTime = this.getCurrentSeconds();

    public void finishTask(long rt) {
        rtList.add(rt);
    }

    public PerformanceSnapshot getSnapshotAndReset() {
        long currentSeconds = getCurrentSeconds();
        int monitorInterval = (int) (currentSeconds - lastRefreshTime);
        reset(currentSeconds);
        return new PerformanceSnapshot(monitorInterval);
    }

    private void reset(long currentSeconds) {
        rtList.clear();
        lastRefreshTime = currentSeconds;
    }

    private long getCurrentSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    @Getter
    public class PerformanceSnapshot {

        private final double tps;

        private final double mean;

        private final long maxRt;

        private final long minRt;

        private final double median;

        private final double tp75;

        private final double tp90;

        private final double tp95;

        private final double tp99;

        private final double tp999;

        public PerformanceSnapshot(int monitorInterval) {
            Snapshot tpSnapshot = new Snapshot(rtList);
            tps = BigDecimal.valueOf(tpSnapshot.size()).divide(BigDecimal.valueOf(Math.max(monitorInterval, 1)),
                    1, RoundingMode.HALF_UP).doubleValue();
            mean = tpSnapshot.getMean();
            maxRt = tpSnapshot.getMax();
            minRt = tpSnapshot.getMin();
            median = tpSnapshot.getMedian();
            tp75 = tpSnapshot.get75thPercentile();
            tp90 = tpSnapshot.getValue(0.9);
            tp95 = tpSnapshot.get95thPercentile();
            tp99 = tpSnapshot.get99thPercentile();
            tp999 = tpSnapshot.get999thPercentile();
        }
    }
}
