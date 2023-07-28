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

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.notifier.alarm.ThreadPoolAlarmHelper;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * ExecutorAlarmAware related
 *
 * @author kyao
 * @Since 1.1.4
 */
@Slf4j
public class ExecutorAlarmAware implements ExecutorAware {

    private final Map<Executor, ThreadPoolAlarmHelper> alarmHelperMap = new ConcurrentHashMap<>();

    public void addAlarmHelper(Executor executor, ThreadPoolAlarmHelper alarmHelper) {
        alarmHelperMap.put(executor, alarmHelper);
        alarmHelperMap.put(alarmHelper.getExecutorWrapper().getExecutor(), alarmHelper);
        alarmHelperMap.put(alarmHelper.getExecutorWrapper().getExecutor().getOriginal(), alarmHelper);
    }

    public ThreadPoolAlarmHelper getAlarmHelper(Executor executor) {
        return alarmHelperMap.get(executor);
    }

    @Override
    public void executeEnhance(Executor executor, Runnable r) {
        Optional.ofNullable(alarmHelperMap.get(executor)).ifPresent(alarmHelper -> alarmHelper.startQueueTimeoutTask(r));
    }

    @Override
    public void beforeExecuteEnhance(Executor executor, Thread t, Runnable r) {
        Optional.ofNullable(alarmHelperMap.get(executor)).ifPresent(alarmHelper -> {
            alarmHelper.cancelQueueTimeoutTask(r);
            alarmHelper.startRunTimeoutTask(t, r);
        });

    }

    @Override
    public void afterExecuteEnhance(Executor executor, Runnable r, Throwable t) {
        Optional.ofNullable(alarmHelperMap.get(executor)).ifPresent(alarmHelper -> alarmHelper.cancelRunTimeoutTask(r));
    }
}
