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

import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.common.entity.TpMainFields;
import org.dromara.dynamictp.core.support.ExecutorAdapter;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import lombok.val;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * ExecutorConverter related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public class ExecutorConverter {

    private ExecutorConverter() { }

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
        ThreadPoolStats poolStats = convertCommon(executor);
        poolStats.setPoolName(wrapper.getThreadPoolName());
        Optional.ofNullable(wrapper.getThreadPoolStatProvider()).ifPresent(p -> {
            poolStats.setRunTimeoutCount(p.getRunTimeoutCount());
            poolStats.setQueueTimeoutCount(p.getQueueTimeoutCount());
            poolStats.setRejectCount(p.getRejectedTaskCount());
        });
        poolStats.setDynamic(executor instanceof DtpExecutor);
        return poolStats;
    }

    private static ThreadPoolStats convertCommon(ExecutorAdapter<?> executor) {
        return ThreadPoolStats.builder()
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(executor.getMaximumPoolSize())
                .poolSize(executor.getPoolSize())
                .activeCount(executor.getActiveCount())
                .largestPoolSize(executor.getLargestPoolSize())
                .queueType(executor.getQueueType())
                .queueCapacity(executor.getQueueCapacity())
                .queueSize(executor.getQueueSize())
                .queueRemainingCapacity(executor.getQueueRemainingCapacity())
                .taskCount(executor.getTaskCount())
                .completedTaskCount(executor.getCompletedTaskCount())
                .waitTaskCount(executor.getQueueSize())
                .rejectHandlerName(executor.getRejectHandlerType())
                .build();
    }
}
