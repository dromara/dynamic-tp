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

import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.timer.Timeout;
import org.dromara.dynamictp.common.timer.TimerTask;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.task.runnable.DtpRunnable;
import org.dromara.dynamictp.core.thread.DtpExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A timer task used to handle run timeout.
 *
 * @author kamtohung
 **/
@Slf4j
public class RunTimeoutTimerTask implements TimerTask {

    private final ExecutorWrapper executorWrapper;

    private final Runnable runnable;

    private final Thread thread;

    private final List<NotifyItemEnum> notifyItemEnumList = Collections.singletonList(NotifyItemEnum.RUN_TIMEOUT);

    public RunTimeoutTimerTask(ExecutorWrapper executorWrapper, Runnable runnable, Thread thread) {
        this.executorWrapper = executorWrapper;
        this.runnable = runnable;
        this.thread = thread;
    }

    @Override
    public void run(Timeout timeout) {
        Optional.ofNullable(executorWrapper.getThreadPoolAlarmHelper()).ifPresent(s -> s.incRunTimeoutCount(1));
        if (executorWrapper.getExecutor() instanceof DtpExecutor &&
                runnable instanceof DtpRunnable) {
            DtpRunnable dtpRunnable = (DtpRunnable) runnable;
            DtpExecutor dtpExecutor = (DtpExecutor) executorWrapper.getExecutor();
            AlarmManager.doAlarmAsync(dtpExecutor, NotifyItemEnum.RUN_TIMEOUT, runnable);
            log.warn("DynamicTp execute, run timeout, tpName: {}, taskName: {}, traceId: {}, stackTrace: {}",
                    dtpExecutor.getThreadPoolName(), dtpRunnable.getTaskName(),
                    dtpRunnable.getTraceId(), traceToString(thread.getStackTrace()));
        } else {
            AlarmManager.doAlarmAsync(this.executorWrapper, notifyItemEnumList);
            log.warn("DynamicTp execute, run timeout, tpName: {}, stackTrace: {}",
                    this.executorWrapper.getThreadPoolName(), traceToString(thread.getStackTrace()));
        }

    }

    public String traceToString(StackTraceElement[] trace) {
        StringBuilder builder = new StringBuilder(512);
        builder.append("\n");
        for (StackTraceElement traceElement : trace) {
            builder.append("\tat ").append(traceElement).append("\n");
        }
        return builder.toString();
    }
}
