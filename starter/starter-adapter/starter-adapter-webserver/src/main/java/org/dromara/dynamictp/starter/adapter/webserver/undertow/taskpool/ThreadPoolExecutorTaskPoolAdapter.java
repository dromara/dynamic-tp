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

package org.dromara.dynamictp.starter.adapter.webserver.undertow.taskpool;

import org.dromara.dynamictp.starter.adapter.webserver.undertow.UndertowTaskPoolEnum;
import org.dromara.dynamictp.core.support.adapter.ExecutorAdapter;
import org.dromara.dynamictp.core.support.adapter.ThreadPoolExecutorAdapter;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * ThreadPoolExecutorTaskPoolHandler related
 *
 * @author yanhom
 * @since 1.1.3
 */
public class ThreadPoolExecutorTaskPoolAdapter implements TaskPoolAdapter {

    @Override
    public UndertowTaskPoolEnum taskPoolType() {
        return UndertowTaskPoolEnum.THREAD_POOL_EXECUTOR_TASK_POOL;
    }

    @Override
    public ExecutorAdapter<ThreadPoolExecutor> adapt(Object executor) {
        return new ThreadPoolExecutorAdapter((ThreadPoolExecutor) executor);
    }
}
