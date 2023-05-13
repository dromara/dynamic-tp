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

package org.dromara.dynamictp.core.reject;

import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.support.task.runnable.DtpRunnable;
import org.dromara.dynamictp.core.thread.DtpExecutor;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.concurrent.ThreadPoolExecutor;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.TRACE_ID;
import static org.dromara.dynamictp.common.em.NotifyItemEnum.REJECT;

/**
 * RejectedAware related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public interface RejectedAware {

    /**
     * Do sth before reject.
     *
     * @param runnable Runnable instance
     * @param executor ThreadPoolExecutor instance
     * @param log      logger
     */
    default void beforeReject(Runnable runnable, ThreadPoolExecutor executor, Logger log) {
        if (executor instanceof DtpExecutor) {
            val dtpRunnable = (DtpRunnable) runnable;
            dtpRunnable.cancelQueueTimeoutTask();
            DtpExecutor dtpExecutor = (DtpExecutor) executor;
            dtpExecutor.incRejectCount(1);
            AlarmManager.doAlarmAsync(dtpExecutor, REJECT);
            log.warn("DynamicTp execute, thread pool is exhausted, tpName: {}, taskName: {}, traceId: {}, " +
                            "poolSize: {} (active: {}, core: {}, max: {}, largest: {}), " +
                            "task: {} (completed: {}), queueCapacity: {}, (currSize: {}, remaining: {}), " +
                            "executorStatus: (isShutdown: {}, isTerminated: {}, isTerminating: {})",
                    dtpExecutor.getThreadPoolName(), dtpRunnable.getTaskName(), MDC.get(TRACE_ID), executor.getPoolSize(),
                    executor.getActiveCount(), executor.getCorePoolSize(), executor.getMaximumPoolSize(),
                    executor.getLargestPoolSize(), executor.getTaskCount(), executor.getCompletedTaskCount(),
                    dtpExecutor.getQueueCapacity(), dtpExecutor.getQueue().size(), executor.getQueue().remainingCapacity(),
                    executor.isShutdown(), executor.isTerminated(), executor.isTerminating());
        }
    }
}
