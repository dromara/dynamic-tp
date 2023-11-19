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

import org.dromara.dynamictp.common.entity.TpExecutorProps;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolStatProvider;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * TaskStatAware related
 *
 * @author kyao
 * @since 1.1.4
 */
public abstract class TaskStatAware implements ExecutorAware {

    protected final Map<Executor, ThreadPoolStatProvider> statProviders = new ConcurrentHashMap<>();

    @Override
    public void register(ExecutorWrapper wrapper) {
        ThreadPoolStatProvider statProvider = wrapper.getThreadPoolStatProvider();
        statProviders.put(wrapper.getExecutor(), statProvider);
        statProviders.put(wrapper.getExecutor().getOriginal(), statProvider);
    }

    @Override
    public void refresh(ExecutorWrapper wrapper, TpExecutorProps props) {
        if (Objects.isNull(statProviders.get(wrapper.getExecutor()))) {
            register(wrapper);
        }
        ThreadPoolStatProvider statProvider = wrapper.getThreadPoolStatProvider();
        refresh(props, statProvider);
    }

    @Override
    public void remove(ExecutorWrapper wrapper) {
        statProviders.remove(wrapper.getExecutor());
        statProviders.remove(wrapper.getExecutor().getOriginal());
    }

    protected void refresh(TpExecutorProps props, ThreadPoolStatProvider statProvider) { }
}
