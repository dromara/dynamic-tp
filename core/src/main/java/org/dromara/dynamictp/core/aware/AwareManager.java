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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.entity.TpExecutorProps;
import org.dromara.dynamictp.common.util.ExtensionServiceLoader;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

    private AwareManager() { }

    static {
        EXECUTOR_AWARE_LIST.add(new TaskTimeoutAware());
        EXECUTOR_AWARE_LIST.add(new TaskRejectAware());

        List<ExecutorAware> serviceLoader = ExtensionServiceLoader.get(ExecutorAware.class);
        EXECUTOR_AWARE_LIST.addAll(serviceLoader);
        EXECUTOR_AWARE_LIST.sort(Comparator.comparingInt(ExecutorAware::getOrder));
    }

    public static void addExecutorAware(ExecutorAware aware) {
        for (ExecutorAware executorAware : EXECUTOR_AWARE_LIST) {
            if (executorAware.getClass().equals(aware.getClass())) {
                return;
            }
        }
        EXECUTOR_AWARE_LIST.add(aware);
        EXECUTOR_AWARE_LIST.sort(Comparator.comparingInt(ExecutorAware::getOrder));
    }

    public static void register(ExecutorWrapper executorWrapper) {
        for (ExecutorAware executorAware : EXECUTOR_AWARE_LIST) {
            executorAware.updateInfo(executorWrapper, null);
        }
    }

    public static void updateTpInfo(ExecutorWrapper executorWrapper, TpExecutorProps props) {
        for (ExecutorAware executorAware : EXECUTOR_AWARE_LIST) {
            if (CollectionUtil.isEmpty(props.getAwareTypes()) || props.getAwareTypes().contains(executorAware.getName())) {
                executorAware.updateInfo(executorWrapper, props);
            } else {
                executorAware.remove(executorWrapper);
            }
        }
    }

    public static void executeEnhance(Executor executor, Runnable r) {
        for (ExecutorAware aware : EXECUTOR_AWARE_LIST) {
            try {
                aware.executeEnhance(executor, r);
            } catch (Exception e) {
                log.error(StrUtil.format("AwareName:{} executeEnhance exception", aware.getName()), e);
            }
        }
    }

    public static void beforeExecuteEnhance(Executor executor, Thread t, Runnable r) {
        for (ExecutorAware aware : EXECUTOR_AWARE_LIST) {
            try {
                aware.beforeExecuteEnhance(executor, t, r);
            } catch (Exception e) {
                log.error(StrUtil.format("AwareName:{} beforeExecuteEnhance exception", aware.getName()), e);
            }
        }
    }

    public static void afterExecuteEnhance(Executor executor, Runnable r, Throwable t) {
        for (ExecutorAware aware : EXECUTOR_AWARE_LIST) {
            try {
                aware.afterExecuteEnhance(executor, r, t);
            } catch (Exception e) {
                log.error(StrUtil.format("AwareName:{} afterExecuteEnhance exception", aware.getName()), e);
            }
        }
    }

    public static void beforeReject(Runnable r, Executor executor, Logger log) {
        for (ExecutorAware aware : EXECUTOR_AWARE_LIST) {
            try {
                aware.beforeReject(r, executor, log);
            } catch (Exception e) {
                log.error(StrUtil.format("AwareName:{} beforeReject exception", aware.getName()), e);
            }
        }
    }
}
