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

package org.dromara.dynamictp.starter.adapter.webserver.undertow;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * UndertowTaskPoolEnum related
 *
 * @author yanhom
 * @since 1.1.3
 */
@Getter
@AllArgsConstructor
public enum UndertowTaskPoolEnum {

    /**
     * EnhancedQueueExecutorTaskPool
     */
    ENHANCED_QUEUE_EXECUTOR_TASK_POOL("EnhancedQueueExecutorTaskPool", "executor"),

    /**
     * ThreadPoolExecutorTaskPool
     */
    THREAD_POOL_EXECUTOR_TASK_POOL("ThreadPoolExecutorTaskPool", "delegate"),

    /**
     * ExternalTaskPool
     */
    EXTERNAL_TASK_POOL("ExternalTaskPool", "delegate"),

    /**
     * ExecutorServiceTaskPool
     */
    EXECUTOR_SERVICE_TASK_POOL("ExecutorServiceTaskPool", "delegate");

    private final String className;

    private final String internalExecutor;

}
