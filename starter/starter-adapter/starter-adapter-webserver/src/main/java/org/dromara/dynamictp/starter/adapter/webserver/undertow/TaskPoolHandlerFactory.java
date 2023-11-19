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

import org.dromara.dynamictp.starter.adapter.webserver.undertow.taskpool.EnhancedQueueExecutorTaskPoolAdapter;
import org.dromara.dynamictp.starter.adapter.webserver.undertow.taskpool.ExecutorServiceTaskPoolAdapter;
import org.dromara.dynamictp.starter.adapter.webserver.undertow.taskpool.ExternalTaskPoolAdapter;
import org.dromara.dynamictp.starter.adapter.webserver.undertow.taskpool.TaskPoolAdapter;
import org.dromara.dynamictp.starter.adapter.webserver.undertow.taskpool.ThreadPoolExecutorTaskPoolAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * TaskPoolHandlerFactory related
 *
 * @author yanhom
 * @since 1.1.3
 */
public class TaskPoolHandlerFactory {

    private TaskPoolHandlerFactory() { }

    private static final Map<String, TaskPoolAdapter> TASK_POOL_HANDLERS = new HashMap<>();

    static {
        TASK_POOL_HANDLERS.put(UndertowTaskPoolEnum.EXTERNAL_TASK_POOL.getClassName(), new ExternalTaskPoolAdapter());
        TASK_POOL_HANDLERS.put(UndertowTaskPoolEnum.ENHANCED_QUEUE_EXECUTOR_TASK_POOL.getClassName(), new EnhancedQueueExecutorTaskPoolAdapter());
        TASK_POOL_HANDLERS.put(UndertowTaskPoolEnum.THREAD_POOL_EXECUTOR_TASK_POOL.getClassName(), new ThreadPoolExecutorTaskPoolAdapter());
        TASK_POOL_HANDLERS.put(UndertowTaskPoolEnum.EXECUTOR_SERVICE_TASK_POOL.getClassName(), new ExecutorServiceTaskPoolAdapter());
    }

    public static TaskPoolAdapter getTaskPoolHandler(String className) {
        return TASK_POOL_HANDLERS.get(className);
    }
}
