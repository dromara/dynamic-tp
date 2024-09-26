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

package org.dromara.dynamictp.common.entity;

import lombok.Data;
import org.dromara.dynamictp.common.constant.DynamicTpConst;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.em.RejectedTypeEnum;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * ThreadPool base properties, mainly used for adapter module.
 *
 * @author yanhom
 * @since 1.0.6
 **/
@Data
public class TpExecutorProps {

    /**
     * Name of ThreadPool.
     */
    private String threadPoolName;

    /**
     * Simple Alias Name of  ThreadPool. Use for notify.
     */
    private String threadPoolAliasName;

    /**
     * Thread name prefix.
     */
    private String threadNamePrefix = "dtp";

    /**
     * CoreSize of ThreadPool.
     */
    private int corePoolSize = 1;

    /**
     * MaxSize of ThreadPool.
     */
    private int maximumPoolSize = DynamicTpConst.AVAILABLE_PROCESSORS;

    /**
     * When the number of threads is greater than the core,
     * this is the maximum time that excess idle threads
     * will wait for new tasks before terminating.
     */
    private long keepAliveTime = 60;

    /**
     * Timeout unit.
     */
    private TimeUnit unit = TimeUnit.SECONDS;

    /**
     * BlockingQueue capacity.
     */
    private int queueCapacity = 1024;

    /**
     * Max free memory for MemorySafeLBQ, unit M
     */
    private int maxFreeMemory = 16;

    /**
     * RejectedExecutionHandler type, see {@link RejectedTypeEnum}
     */
    private String rejectedHandlerType = RejectedTypeEnum.ABORT_POLICY.getName();

    /**
     * If enhance reject.
     */
    private boolean rejectEnhanced = true;

    /**
     * If allow core thread timeout.
     */
    private boolean allowCoreThreadTimeOut = false;

    /**
     * Notify items, see {@link NotifyItemEnum}
     */
    private List<NotifyItem> notifyItems;

    /**
     * Notify platform id
     */
    private List<String> platformIds;

    /**
     * If enable notify.
     */
    private boolean notifyEnabled = true;

    /**
     * Task execute timeout, unit (ms).
     */
    private long runTimeout = 0;

    /**
     * If try interrupt thread when run timeout.
     */
    private boolean tryInterrupt = false;

    /**
     * Task queue wait timeout, unit (ms), just for statistics.
     */
    private long queueTimeout = 0;

    /**
     * Whether to wait for scheduled tasks to complete on shutdown,
     * not interrupting running tasks and executing all tasks in the queue.
     */
    private boolean waitForTasksToCompleteOnShutdown = true;

    /**
     * The maximum number of seconds that this executor is supposed to block
     * on shutdown in order to wait for remaining tasks to complete their execution
     * before the rest of the container continues to shut down.
     */
    private int awaitTerminationSeconds = 3;

    /**
     * Task wrapper names.
     */
    private Set<String> taskWrapperNames;

    /**
     * Aware names.
     */
    private List<String> awareNames;

    /**
     * check core param is inValid
     *
     * @return boolean return true means params is inValid
     */
    public boolean coreParamIsInValid() {
        return this.getCorePoolSize() < 0
                || this.getMaximumPoolSize() <= 0
                || this.getMaximumPoolSize() < this.getCorePoolSize()
                || this.getKeepAliveTime() < 0;
    }
}
