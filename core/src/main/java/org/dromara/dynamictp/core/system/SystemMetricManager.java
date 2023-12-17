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

package org.dromara.dynamictp.core.system;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.support.ThreadPoolCreator;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Refer to sentinel, @see <a href="https://github.com/alibaba/Sentinel/blob/master/sentinel-core/src/main/java/com/alibaba/csp/sentinel/slots/system/SystemStatusListener.java">SystemStatusListener</a>
 *
 * @author yanhom
 * @since 1.1.5
 */
@Slf4j
public class SystemMetricManager {

    private static final CpuMetricsCaptor CPU_METRICS_CAPTOR;

    private static final MemoryMetricsCaptor MEMORY_METRICS_CAPTOR;

    private static final ScheduledExecutorService EXECUTOR = ThreadPoolCreator.newScheduledThreadPool("dtp-system-metric", 1);

    static {
        CPU_METRICS_CAPTOR = new CpuMetricsCaptor();
        MEMORY_METRICS_CAPTOR = new MemoryMetricsCaptor();
        EXECUTOR.scheduleAtFixedRate(CPU_METRICS_CAPTOR, 0, 2, TimeUnit.SECONDS);
        EXECUTOR.scheduleAtFixedRate(MEMORY_METRICS_CAPTOR, 0, 2, TimeUnit.SECONDS);
    }

    public static String getSystemMetric() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double systemAvgLoad = osBean.getSystemLoadAverage();
        double systemCpuUsage = OperatingSystemBeanManager.getSystemCpuUsage();
        int cpuCores = osBean.getAvailableProcessors();
        return String.format("SystemMetric{sAvgLoad=%.2f, sCpuUsage=%.2f, pCpuUsage=%.2f, cpuCores=%d, oldMemUsage=%.2f}",
                systemAvgLoad, systemCpuUsage, getProcessCpuUsage(), cpuCores, getLongLivedMemoryUsage());
    }

    public static double getProcessCpuUsage() {
        return CPU_METRICS_CAPTOR.getProcessCpuUsage();
    }

    public static double getLongLivedMemoryUsage() {
        return MEMORY_METRICS_CAPTOR.getLongLivedMemoryUsage();
    }

    public static void stop() {
        EXECUTOR.shutdown();
    }
}
