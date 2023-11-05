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
import org.dromara.dynamictp.core.executor.NamedThreadFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Refer to sentinel, @see <a href="https://github.com/alibaba/Sentinel/blob/master/sentinel-core/src/main/java/com/alibaba/csp/sentinel/slots/system/SystemStatusListener.java">SystemStatusListener</a>
 *
 * @author yanhom
 * @since 1.1.5
 */
@Slf4j
public class SystemMetricManager {

    private static final SystemMetricPoller METRIC_POLLER;

    private static final ScheduledExecutorService EXECUTOR = new ScheduledThreadPoolExecutor(
            1, new NamedThreadFactory("system-metric", true));

    static {
        METRIC_POLLER = new SystemMetricPoller();
        EXECUTOR.scheduleAtFixedRate(METRIC_POLLER, 0, 2, TimeUnit.SECONDS);
    }

    public static String getSystemMetric() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double systemAvgLoad = osBean.getSystemLoadAverage();
        double systemCpuUsage = OperatingSystemBeanManager.getSystemCpuUsage();
        int cpuCores = osBean.getAvailableProcessors();
        return String.format("SystemMetric{sAvgLoad=%.2f, sCpuUsage=%.2f, pCpuUsage=%.2f, cpuCores=%d}",
                systemAvgLoad, systemCpuUsage, getProcessCpuUsage(), cpuCores);
    }

    public static double getProcessCpuUsage() {
        return METRIC_POLLER.getProcessCpuUsage();
    }

    public static void stop() {
        EXECUTOR.shutdown();
    }

    private static class SystemMetricPoller implements Runnable {

        private double currProcessCpuUsage = -1;

        private long prevProcessCpuTime = 0;

        private long prevUpTime = 0;

        public double getProcessCpuUsage() {
            return currProcessCpuUsage;
        }

        @Override
        public void run() {
            try {
                OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
                int cpuCores = osBean.getAvailableProcessors();

                long newProcessCpuTime = OperatingSystemBeanManager.getProcessCpuTime();
                RuntimeMXBean runtimeBean = ManagementFactory.getPlatformMXBean(RuntimeMXBean.class);
                long newUpTime = runtimeBean.getUptime();
                long elapsedCpu = TimeUnit.NANOSECONDS.toMillis(newProcessCpuTime - prevProcessCpuTime);
                long elapsedTime = newUpTime - prevUpTime;
                double processCpuUsage = (double) elapsedCpu / elapsedTime / cpuCores;
                prevProcessCpuTime = newProcessCpuTime;
                prevUpTime = newUpTime;
                currProcessCpuUsage = Math.min(processCpuUsage, 1);
            } catch (Throwable e) {
                log.warn("Get system metrics error.", e);
            }
        }
    }
}
