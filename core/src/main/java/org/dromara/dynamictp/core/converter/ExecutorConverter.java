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

package org.dromara.dynamictp.core.converter;

import lombok.val;
import org.dromara.dynamictp.common.entity.ExecutorStats;
import org.dromara.dynamictp.common.entity.TpMainFields;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.monitor.PerformanceProvider;
import org.dromara.dynamictp.core.support.adapter.ExecutorAdapter;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ExecutorStatProvider;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ExecutorConverter related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public class ExecutorConverter {

    private ExecutorConverter() {
    }

    public static TpMainFields toMainFields(ExecutorWrapper executorWrapper) {
        TpMainFields mainFields = new TpMainFields();
        mainFields.setThreadPoolName(executorWrapper.getThreadPoolName());
        val executor = executorWrapper.getExecutor();
        mainFields.setCorePoolSize(executor.getCorePoolSize());
        mainFields.setMaxPoolSize(executor.getMaximumPoolSize());
        mainFields.setKeepAliveTime(executor.getKeepAliveTime(TimeUnit.SECONDS));
        mainFields.setQueueType(executor.getQueueType());
        mainFields.setQueueCapacity(executor.getQueueCapacity());
        mainFields.setAllowCoreThreadTimeOut(executor.allowsCoreThreadTimeOut());
        mainFields.setRejectType(executor.getRejectHandlerType());
        return mainFields;
    }

    public static ExecutorStats toMetrics(ExecutorWrapper wrapper) {
        ExecutorAdapter<?> executor = wrapper.getExecutor();
        if (executor == null) {
            return null;
        }
        ExecutorStatProvider provider = wrapper.getExecutorStatProvider();
        PerformanceProvider performanceProvider = provider.getPerformanceProvider();
        val performanceSnapshot = performanceProvider.getSnapshotAndReset();
        ExecutorStats executorStats = convertCommon(executor);
        executorStats.setExecutorName(wrapper.getThreadPoolName());
        executorStats.setExecutorAliasName(wrapper.getThreadPoolAliasName());
        executorStats.setRunTimeoutCount(provider.getRunTimeoutCount());
        executorStats.setQueueTimeoutCount(provider.getQueueTimeoutCount());
        executorStats.setRejectCount(provider.getRejectedTaskCount());

        executorStats.setVirtualThreadExecutor(wrapper.isVirtualThreadExecutor());
        toVirtualThreadMetrics(executorStats);

        executorStats.setDynamic(executor instanceof DtpExecutor);
        executorStats.setTps(performanceSnapshot.getTps());
        executorStats.setAvg(performanceSnapshot.getAvg());
        executorStats.setMaxRt(performanceSnapshot.getMaxRt());
        executorStats.setMinRt(performanceSnapshot.getMinRt());
        executorStats.setTp50(performanceSnapshot.getTp50());
        executorStats.setTp75(performanceSnapshot.getTp75());
        executorStats.setTp90(performanceSnapshot.getTp90());
        executorStats.setTp95(performanceSnapshot.getTp95());
        executorStats.setTp99(performanceSnapshot.getTp99());
        executorStats.setTp999(performanceSnapshot.getTp999());
        return executorStats;
    }

    private static void toVirtualThreadMetrics(ExecutorStats executorStats) {
        if (!executorStats.isVirtualThreadExecutor()) {
            return;
        }
        Map<String, Number> vteStats = PerformanceProvider.getVteStat(executorStats.getExecutorName());
        executorStats.getExtMap().put("maxPinnedTime", (Double) vteStats.getOrDefault("maxPinnedTime", 0d));
        executorStats.getExtMap().put("totalPinnedTime", (Double) vteStats.getOrDefault("totalPinnedTime", 0d));
    }

    private static ExecutorStats convertCommon(ExecutorAdapter<?> executor) {
        ExecutorStats executorStats = new ExecutorStats();
        executorStats.setCorePoolSize(executor.getCorePoolSize());
        executorStats.setMaximumPoolSize(executor.getMaximumPoolSize());
        executorStats.setPoolSize(executor.getPoolSize());
        executorStats.setActiveCount(executor.getActiveCount());
        executorStats.setLargestPoolSize(executor.getLargestPoolSize());
        executorStats.setQueueType(executor.getQueueType());
        executorStats.setQueueCapacity(executor.getQueueCapacity());
        executorStats.setQueueSize(executor.getQueueSize());
        executorStats.setQueueRemainingCapacity(executor.getQueueRemainingCapacity());
        executorStats.setTaskCount(executor.getTaskCount());
        executorStats.setCompletedTaskCount(executor.getCompletedTaskCount());
        executorStats.setWaitTaskCount(executor.getQueueSize());
        executorStats.setRejectHandlerName(executor.getRejectHandlerType());
        executorStats.setKeepAliveTime(executor.getKeepAliveTime(TimeUnit.MILLISECONDS));
        return executorStats;
    }
}
