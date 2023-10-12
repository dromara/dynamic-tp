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

package org.dromara.dynamictp.core.support;

import lombok.Getter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TpPerformanceProvider related
 *
 * @author kyao
 * @date 2023年09月24日 14:28
 */
public class TpPerformanceProvider {

    /**
     * 任务执行时间集合
     */
    private final CopyOnWriteArrayList<Long> finishTimeList = new CopyOnWriteArrayList<>();

    /**
     * 上一次刷新数据时间
     */
    private long lastRefreshTime = this.getCurrentSeconds();

    public void finishTask(long finishTime) {
        finishTimeList.add(finishTime);
    }

    public PerformanceSnapshot getSnapshotAndRefresh() {
        long currentSeconds = getCurrentSeconds();
        int monitorInterval = (int) (currentSeconds - lastRefreshTime);
        PerformanceSnapshot performanceSnapshot = new PerformanceSnapshot(monitorInterval);
        refresh(currentSeconds);
        return performanceSnapshot;
    }

    private void refresh(long currentSeconds) {
        finishTimeList.clear();
        lastRefreshTime = currentSeconds;
    }

    private long getCurrentSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    @Getter
    public class PerformanceSnapshot {

        private final double tps;

        private final double completedTaskTimeAvg;

        private final long maxRt;

        private final long minRt;

        private final double tp75;

        private final double tp90;

        private final double tp95;

        private final double tp99;

        public PerformanceSnapshot(int monitorInterval) {
            int completedTaskNum = finishTimeList.size();
            tps = BigDecimal.valueOf(completedTaskNum).divide(BigDecimal.valueOf(Math.max(monitorInterval, 1)), 1, RoundingMode.HALF_UP).doubleValue();
            Snapshot tpSnapshot = new Snapshot(finishTimeList);
            completedTaskTimeAvg = tpSnapshot.getMean();
            maxRt = tpSnapshot.getMax();
            minRt = tpSnapshot.getMin();
            tp75 = tpSnapshot.get75thPercentile();
            tp90 = tpSnapshot.getValue(0.9);
            tp95 = tpSnapshot.get95thPercentile();
            tp99 = tpSnapshot.get99thPercentile();
        }

    }

}
