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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TpPerformanceProvider related
 *
 * @author kyao
 * @date 2023年09月24日 14:28
 */
public class TpPerformanceProvider {

    /**
     * 完成任务数量
     */
    private final AtomicInteger completedTaskNum = new AtomicInteger(0);

    /**
     * 完成任务的时间总和(单位:ms)
     */
    private final AtomicLong finishTimeTotal = new AtomicLong(0);

    /**
     * 上一次刷新数据时间
     */
    private long lastRefreshTime = this.getCurrentSeconds();

    public void finishTask(long finishTime) {
        completedTaskNum.incrementAndGet();
        finishTimeTotal.addAndGet(finishTime);
    }

    public PerformanceData getDataAndRefresh() {
        long currentSeconds = getCurrentSeconds();
        int monitorInterval = (int) (currentSeconds - lastRefreshTime);
        PerformanceData performanceData = new PerformanceData(monitorInterval);
        refresh(currentSeconds);
        return performanceData;
    }

    private void refresh(long currentSeconds) {
        completedTaskNum.set(0);
        finishTimeTotal.set(0L);
        lastRefreshTime = currentSeconds;
    }

    private long getCurrentSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    @Getter
    public class PerformanceData {

        private final int tps;

        private final int completedTaskTimeAvg;

        public PerformanceData(int monitorInterval) {
            tps = Math.floorDiv(completedTaskNum.get(), Math.max(monitorInterval, 1));
            completedTaskTimeAvg = (int) Math.floorDiv(finishTimeTotal.get(), Math.max(completedTaskNum.get(), 1));
        }

    }

}
