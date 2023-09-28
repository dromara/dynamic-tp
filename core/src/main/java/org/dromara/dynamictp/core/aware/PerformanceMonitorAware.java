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

import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolStatProvider;
import org.dromara.dynamictp.core.support.TpPerformanceProvider;
import org.springframework.util.StopWatch;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * PerformanceMonitorAware related
 *
 * @author kyao
 * @date 2023年09月24日 15:36
 */
public class PerformanceMonitorAware implements ExecutorAware {

    private final Map<Executor, TpPerformanceProvider> performanceProviders = new ConcurrentHashMap<>();

    private final Map<Runnable, SoftReference<StopWatch>> stopWatches = new ConcurrentHashMap<>();

    @Override
    public int getOrder() {
        return AwareTypeEnum.PERFORMANCE_MONITOR_AWARE.getOrder();
    }

    @Override
    public String getName() {
        return AwareTypeEnum.PERFORMANCE_MONITOR_AWARE.getName();
    }

    @Override
    public void beforeExecute(Executor executor, Thread t, Runnable r) {
        StopWatch stopWatch = new StopWatch();
        SoftReference<StopWatch> stopWatchSoftReference = new SoftReference<>(stopWatch);
        stopWatches.put(r, stopWatchSoftReference);
        stopWatch.start();
    }

    @Override
    public void afterExecute(Executor executor, Runnable r, Throwable t) {
        Optional.ofNullable(stopWatches.get(r).get()).ifPresent(stopWatch -> {
            stopWatch.stop();
            long totalTimeMillis = stopWatch.getTotalTimeMillis();
            Optional.ofNullable(performanceProviders.get(executor)).ifPresent(provider ->
                    provider.finishTask(totalTimeMillis)
            );
        });
        stopWatches.remove(r);
    }

    @Override
    public void register(ExecutorWrapper wrapper) {
        ThreadPoolStatProvider statProvider = wrapper.getThreadPoolStatProvider();
        performanceProviders.put(wrapper.getExecutor(), statProvider.getPerformanceProvider());
        performanceProviders.put(wrapper.getExecutor().getOriginal(), statProvider.getPerformanceProvider());
    }

    @Override
    public void remove(ExecutorWrapper wrapper) {
        performanceProviders.remove(wrapper.getExecutor());
        performanceProviders.remove(wrapper.getExecutor().getOriginal());
    }

}
