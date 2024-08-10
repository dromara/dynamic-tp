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
     * If try interrupt thread when run timeout.
     */
    private boolean tryInterrupt = false;

    /**
     * Alarm threshold, equal to runTimeout and queueTimeout.
     */
    private int threshold = 0;

    /**
     * Task wrapper names.
     */
    private Set<String> taskWrapperNames;

    /**
     * Aware names.
     */
    private List<String> awareNames;
}
