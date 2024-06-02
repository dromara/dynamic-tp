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
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.dynamictp.common.entity.TpExecutorProps;
import org.dromara.dynamictp.common.util.ExtensionServiceLoader;
import org.dromara.dynamictp.core.support.ExecutorWrapper;

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
        EXECUTOR_AWARE_LIST.add(new PerformanceMonitorAware());
        EXECUTOR_AWARE_LIST.add(new TaskTimeoutAware());
        EXECUTOR_AWARE_LIST.add(new TaskRejectAware());

        List<ExecutorAware> serviceLoader = ExtensionServiceLoader.get(ExecutorAware.class);
        EXECUTOR_AWARE_LIST.addAll(serviceLoader);
        EXECUTOR_AWARE_LIST.sort(Comparator.comparingInt(ExecutorAware::getOrder));
    }

    public static void add(ExecutorAware aware) {
        for (ExecutorAware executorAware : EXECUTOR_AWARE_LIST) {
            if (executorAware.getName().equalsIgnoreCase(aware.getName())) {
                return;
            }
        }
        EXECUTOR_AWARE_LIST.add(aware);
        EXECUTOR_AWARE_LIST.sort(Comparator.comparingInt(ExecutorAware::getOrder));
    }

    public static void register(ExecutorWrapper executorWrapper) {
        for (ExecutorAware executorAware : EXECUTOR_AWARE_LIST) {
            val awareNames = executorWrapper.getAwareNames();
            // if awareNames is empty, register all
            if (CollectionUtils.isEmpty(awareNames) || awareNames.contains(executorAware.getName())) {
                executorAware.register(executorWrapper);
            }
        }
    }

    public static void refresh(ExecutorWrapper executorWrapper, TpExecutorProps props) {
        for (ExecutorAware executorAware : EXECUTOR_AWARE_LIST) {
            val awareNames = props.getAwareNames();
            if (CollectionUtils.isEmpty(awareNames) || awareNames.contains(executorAware.getName())) {
                executorAware.refresh(executorWrapper, props);
            } else {
                executorAware.remove(executorWrapper);
            }
        }
    }

    public static void execute(Executor executor, Runnable r) {
        for (ExecutorAware aware : EXECUTOR_AWARE_LIST) {
            try {
                aware.execute(executor, r);
            } catch (Exception e) {
                log.error("DynamicTp aware [{}], enhance execute error.", aware.getName(), e);
            }
        }
    }

    public static void beforeExecute(Executor executor, Thread t, Runnable r) {
        for (ExecutorAware aware : EXECUTOR_AWARE_LIST) {
            try {
                r = aware.beforeExecuteWrap(executor, t, r);
            } catch (Exception e) {
                log.error("DynamicTp aware [{}], enhance beforeExecute error.", aware.getName(), e);
            }
        }
    }

    public static void afterExecute(Executor executor, Runnable r, Throwable t) {
        for (ExecutorAware aware : EXECUTOR_AWARE_LIST) {
            try {
                r = aware.afterExecuteWrap(executor, r, t);
            } catch (Exception e) {
                log.error("DynamicTp aware [{}], enhance afterExecute error.", aware.getName(), e);
            }
        }
    }

    public static void shutdown(Executor executor) {
        for (ExecutorAware aware : EXECUTOR_AWARE_LIST) {
            try {
                aware.shutdown(executor);
            } catch (Exception e) {
                log.error("DynamicTp aware [{}], enhance shutdown error.", aware.getName(), e);
            }
        }
    }

    public static void shutdownNow(Executor executor, List<Runnable> tasks) {
        for (ExecutorAware aware : EXECUTOR_AWARE_LIST) {
            try {
                aware.shutdownNow(executor, tasks);
            } catch (Exception e) {
                log.error("DynamicTp aware [{}], enhance shutdownNow error.", aware.getName(), e);
            }
        }
    }

    public static void terminated(Executor executor) {
        for (ExecutorAware aware : EXECUTOR_AWARE_LIST) {
            try {
                aware.terminated(executor);
            } catch (Exception e) {
                log.error("DynamicTp aware [{}], enhance terminated error.", aware.getName(), e);
            }
        }
    }

    public static void beforeReject(Runnable r, Executor executor) {
        for (ExecutorAware aware : EXECUTOR_AWARE_LIST) {
            try {
                r = aware.beforeRejectWrap(r, executor);
            } catch (Exception e) {
                log.error("DynamicTp aware [{}], enhance beforeReject error.", aware.getName(), e);
            }
        }
    }

    public static void afterReject(Runnable r, Executor executor) {
        for (ExecutorAware aware : EXECUTOR_AWARE_LIST) {
            try {
                r = aware.afterRejectWrap(r, executor);
            } catch (Exception e) {
                log.error("DynamicTp aware [{}], enhance afterReject error.", aware.getName(), e);
            }
        }
    }
}
