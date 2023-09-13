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

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.timer.Timeout;
import org.dromara.dynamictp.common.timer.TimerTask;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.task.runnable.DtpRunnable;

import java.util.Optional;

import static org.dromara.dynamictp.common.em.NotifyItemEnum.QUEUE_TIMEOUT;

/**
 * A timer task used to handle queued timeout.
 *
 * @author kamtohung
 **/
@Slf4j
public class QueueTimeoutTimerTask implements TimerTask {

    private final ExecutorWrapper executorWrapper;

    private final Runnable runnable;

    public QueueTimeoutTimerTask(ExecutorWrapper executorWrapper, Runnable runnable) {
        this.executorWrapper = executorWrapper;
        this.runnable = runnable;
    }

    @Override
    public void run(Timeout timeout) {
        Optional.ofNullable(executorWrapper.getThreadPoolStatProvider()).ifPresent(p -> p.incQueueTimeoutCount(1));
        AlarmManager.doAlarmAsync(executorWrapper, QUEUE_TIMEOUT, runnable);
        if (executorWrapper.isDtpExecutor()) {
            DtpRunnable dtpRunnable = (DtpRunnable) runnable;
            log.warn("DynamicTp execute, queue timeout, tpName: {}, taskName: {}, traceId: {}",
                    executorWrapper.getThreadPoolName(), dtpRunnable.getTaskName(), dtpRunnable.getTraceId());
        } else {
            log.warn("DynamicTp execute, queue timeout, tpName: {}", executorWrapper.getThreadPoolName());
        }
    }
}
