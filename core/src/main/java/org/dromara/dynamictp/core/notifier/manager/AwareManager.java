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

package org.dromara.dynamictp.core.notifier.manager;

import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.spring.ApplicationContextHolder;
import org.dromara.dynamictp.core.aware.ExecutorAlarmAware;
import org.dromara.dynamictp.core.aware.ExecutorAware;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Executor;

/**
 * AwareManager related
 *
 * @author kyao
 * @since 1.1.4
 */
@Slf4j
public class AwareManager {

    private static final List<ExecutorAware> EXECUTOR_AWARE_LIST = new ArrayList<>();

    private AwareManager() {}

    static {
        EXECUTOR_AWARE_LIST.add(new ExecutorAlarmAware());

        ServiceLoader<ExecutorAware> serviceLoader = ServiceLoader.load(ExecutorAware.class);
        for (ExecutorAware aware : serviceLoader) {
            EXECUTOR_AWARE_LIST.add(aware);
        }

        Map<String, ExecutorAware> executorAwareMap = ApplicationContextHolder.getBeansOfType(ExecutorAware.class);
        EXECUTOR_AWARE_LIST.addAll(executorAwareMap.values());
    }

    public static void addExecutorAware(ExecutorAware aware) {
        EXECUTOR_AWARE_LIST.add(aware);
    }

    public static <T extends ExecutorAware> T getExecutorAwareByType(Class<? extends ExecutorAware> clazz) {
        for (ExecutorAware executorAware : EXECUTOR_AWARE_LIST) {
            if (clazz.equals(executorAware.getClass())) {
                return (T) executorAware;
            }
        }

        ExecutorAware executorAware = null;
        try {
            executorAware = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            log.warn(ExceptionUtil.stacktraceToOneLineString(e));
        }
        return (T) executorAware;
    }

    public static void executeEnhance(Executor executor, Runnable r) {
        for (ExecutorAware aware : EXECUTOR_AWARE_LIST) {
            aware.executeEnhance(executor, r);
        }
    }

    public static void beforeExecuteEnhance(Executor executor, Thread t, Runnable r) {
        for (ExecutorAware aware : EXECUTOR_AWARE_LIST) {
            aware.beforeExecuteEnhance(executor, t, r);
        }
    }

    public static void afterExecuteEnhance(Executor executor, Runnable r, Throwable t) {
        for (ExecutorAware aware : EXECUTOR_AWARE_LIST) {
            aware.afterExecuteEnhance(executor, r, t);
        }
    }
}
