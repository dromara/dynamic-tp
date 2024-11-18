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

package org.dromara.dynamictp.core.timer;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.support.adapter.ExecutorAdapter;
import org.dromara.dynamictp.core.support.ExecutorWrapper;

import static org.dromara.dynamictp.common.em.NotifyItemEnum.QUEUE_TIMEOUT;

/**
 * A timer task used to handle queued timeout.
 *
 * @author kamtohung
 **/
@Slf4j
public class QueueTimeoutTimerTask extends AbstractTimeoutTimerTask {

    public QueueTimeoutTimerTask(ExecutorWrapper executorWrapper, Runnable runnable) {
        super(executorWrapper, runnable);
    }

    @Override
    protected void doRun() {
        val statProvider = executorWrapper.getThreadPoolStatProvider();
        ExecutorAdapter<?> executor = statProvider.getExecutorWrapper().getExecutor();
        val pair = getTaskNameAndTraceId();
        statProvider.incQueueTimeoutCount(1);
        AlarmManager.tryAlarmAsync(executorWrapper, QUEUE_TIMEOUT, runnable);
        String logMsg = CharSequenceUtil.format("DynamicTp execute, queue timeout, " +
                        "tpName: {}, taskName: {}, traceId: {}, queueTimeout: {}ms, " +
                        "poolSize: {} (active: {}, core: {}, max: {}, largest: {}), " +
                        "queueCapacity: {} (currSize: {}, remaining: {})",
                statProvider.getExecutorWrapper().getThreadPoolName(), pair.getLeft(), pair.getRight(),
                statProvider.getQueueTimeout(), executor.getPoolSize(), executor.getActiveCount(),
                executor.getCorePoolSize(), executor.getMaximumPoolSize(),
                executor.getLargestPoolSize(), statProvider.getExecutorWrapper().getExecutor().getQueueCapacity(),
                executor.getQueue().size(), executor.getQueue().remainingCapacity());
        log.warn(logMsg);
    }
}
