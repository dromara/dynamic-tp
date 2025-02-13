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
import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.common.entity.TpMainFields;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.monitor.PerformanceProvider;
import org.dromara.dynamictp.core.support.adapter.ExecutorAdapter;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolStatProvider;

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

    public static ThreadPoolStats toMetrics(ExecutorWrapper wrapper) {
        ExecutorAdapter<?> executor = wrapper.getExecutor();
        if (executor == null) {
            return null;
        }
        ThreadPoolStatProvider provider = wrapper.getThreadPoolStatProvider();
        PerformanceProvider performanceProvider = provider.getPerformanceProvider();
        val performanceSnapshot = performanceProvider.getSnapshotAndReset();
        ThreadPoolStats poolStats = convertCommon(executor);
        poolStats.setPoolName(wrapper.getThreadPoolName());
        poolStats.setPoolAliasName(wrapper.getThreadPoolAliasName());
        poolStats.setRunTimeoutCount(provider.getRunTimeoutCount());
        poolStats.setQueueTimeoutCount(provider.getQueueTimeoutCount());
        poolStats.setRejectCount(provider.getRejectedTaskCount());
        poolStats.setDynamic(executor instanceof DtpExecutor);

        poolStats.setTps(performanceSnapshot.getTps());
        poolStats.setAvg(performanceSnapshot.getAvg());
        poolStats.setMaxRt(performanceSnapshot.getMaxRt());
        poolStats.setMinRt(performanceSnapshot.getMinRt());
        poolStats.setTp50(performanceSnapshot.getTp50());
        poolStats.setTp75(performanceSnapshot.getTp75());
        poolStats.setTp90(performanceSnapshot.getTp90());
        poolStats.setTp95(performanceSnapshot.getTp95());
        poolStats.setTp99(performanceSnapshot.getTp99());
        poolStats.setTp999(performanceSnapshot.getTp999());
        return poolStats;
    }

    private static ThreadPoolStats convertCommon(ExecutorAdapter<?> executor) {
        ThreadPoolStats poolStats = new ThreadPoolStats();
        poolStats.setCorePoolSize(executor.getCorePoolSize());
        poolStats.setMaximumPoolSize(executor.getMaximumPoolSize());
        poolStats.setPoolSize(executor.getPoolSize());
        poolStats.setActiveCount(executor.getActiveCount());
        poolStats.setLargestPoolSize(executor.getLargestPoolSize());
        poolStats.setQueueType(executor.getQueueType());
        poolStats.setQueueCapacity(executor.getQueueCapacity());
        poolStats.setQueueSize(executor.getQueueSize());
        poolStats.setQueueRemainingCapacity(executor.getQueueRemainingCapacity());
        poolStats.setTaskCount(executor.getTaskCount());
        poolStats.setCompletedTaskCount(executor.getCompletedTaskCount());
        poolStats.setWaitTaskCount(executor.getQueueSize());
        poolStats.setRejectHandlerName(executor.getRejectHandlerType());
        poolStats.setKeepAliveTime(executor.getKeepAliveTime(TimeUnit.MILLISECONDS));
        return poolStats;
    }
}
