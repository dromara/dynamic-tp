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
import org.dromara.dynamictp.core.support.ThreadPoolStatProvider;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * AlarmAware related
 *
 * @author kyao
 * @since 1.1.4
 */
public abstract class AlarmAware implements ExecutorAware {

    protected final Map<Executor, ThreadPoolStatProvider> alarmHelperMap = new ConcurrentHashMap<>();

    @Override
    public void updateInfo(ExecutorWrapper wrapper, TpExecutorProps props) {
        ThreadPoolStatProvider alarmHelper = wrapper.getAlarmHelper();
        if (Objects.isNull(alarmHelper)) {
            alarmHelper = ThreadPoolStatProvider.of(wrapper);
        }

        if (Objects.nonNull(props)) {
            alarmHelper.setRunTimeout(props.getRunTimeout());
            alarmHelper.setQueueTimeout(props.getQueueTimeout());
        }

        alarmHelperMap.put(wrapper.getExecutor(), alarmHelper);
        alarmHelperMap.put(wrapper.getExecutor().getOriginal(), alarmHelper);
        if (Objects.nonNull(wrapper.getOriginalProxy())) {
            alarmHelperMap.put(wrapper.getOriginalProxy(), alarmHelper);
        }
    }

    @Override
    public void remove(ExecutorWrapper wrapper) {
        alarmHelperMap.remove(wrapper.getExecutor());
        alarmHelperMap.remove(wrapper.getExecutor().getOriginal());
        if (Objects.nonNull(wrapper.getOriginalProxy())) {
            alarmHelperMap.remove(wrapper.getOriginalProxy());
        }
    }

}
