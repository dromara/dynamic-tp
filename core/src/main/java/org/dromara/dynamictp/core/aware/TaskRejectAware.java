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

package org.dromara.dynamictp.core.aware;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.support.adapter.ExecutorAdapter;
import org.dromara.dynamictp.core.support.ThreadPoolStatProvider;
import org.slf4j.MDC;

import java.util.Objects;
import java.util.concurrent.Executor;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.TRACE_ID;
import static org.dromara.dynamictp.common.em.NotifyItemEnum.REJECT;

/**
 * TaskRejectAware related
 *
 * @author kyao
 * @since 1.1.4
 */
@Slf4j
public class TaskRejectAware extends TaskStatAware {

    @Override
    public int getOrder() {
        return AwareTypeEnum.TASK_REJECT_AWARE.getOrder();
    }

    @Override
    public String getName() {
        return AwareTypeEnum.TASK_REJECT_AWARE.getName();
    }

    @Override
    public void beforeReject(Runnable runnable, Executor executor) {
        ThreadPoolStatProvider statProvider = statProviders.get(executor);
        if (Objects.isNull(statProvider)) {
            return;
        }

        statProvider.incRejectCount(1);
        AlarmManager.tryAlarmAsync(statProvider.getExecutorWrapper(), REJECT, runnable);
        ExecutorAdapter<?> executorAdapter = statProvider.getExecutorWrapper().getExecutor();
        String logMsg = CharSequenceUtil.format("DynamicTp execute, thread pool is exhausted, tpName: {},  traceId: {}, " +
                        "poolSize: {} (active: {}, core: {}, max: {}, largest: {}), " +
                        "task: {} (completed: {}), queueCapacity: {}, (currSize: {}, remaining: {}) ," +
                        "executorStatus: (isShutdown: {}, isTerminated: {}, isTerminating: {})",
                statProvider.getExecutorWrapper().getThreadPoolName(), MDC.get(TRACE_ID), executorAdapter.getPoolSize(),
                executorAdapter.getActiveCount(), executorAdapter.getCorePoolSize(), executorAdapter.getMaximumPoolSize(),
                executorAdapter.getLargestPoolSize(), executorAdapter.getTaskCount(), executorAdapter.getCompletedTaskCount(),
                statProvider.getExecutorWrapper().getExecutor().getQueueCapacity(), executorAdapter.getQueue().size(),
                executorAdapter.getQueue().remainingCapacity(),
                executorAdapter.isShutdown(), executorAdapter.isTerminated(), executorAdapter.isTerminating());
        log.warn(logMsg);
    }
}
