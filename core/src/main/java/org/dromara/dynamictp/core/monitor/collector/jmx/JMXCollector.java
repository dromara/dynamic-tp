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

package org.dromara.dynamictp.core.monitor.collector.jmx;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.em.CollectorTypeEnum;
import org.dromara.dynamictp.common.entity.Metrics;
import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.common.entity.VTExecutorStats;
import org.dromara.dynamictp.core.monitor.collector.AbstractCollector;
import org.springframework.beans.BeanUtils;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ThreadPoolStatsInfo related
 *
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
@Slf4j
public class JMXCollector extends AbstractCollector {

    public static final String DTP_METRIC_NAME_PREFIX = "dtp.thread.pool";

    /**
     * 缓存的作用是将注册到JMX的数据，每次都是同一个对象
     */
    private static final Map<String, Metrics> GAUGE_CACHE = new ConcurrentHashMap<>();

    @Override
    public void collect(ThreadPoolStats threadPoolStats) {
        if (GAUGE_CACHE.containsKey(threadPoolStats.getPoolName())) {
            ThreadPoolStats poolStats = (ThreadPoolStats) GAUGE_CACHE.get(threadPoolStats.getPoolName());
            BeanUtils.copyProperties(threadPoolStats, poolStats);
        } else {
            try {
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                ObjectName name = new ObjectName(DTP_METRIC_NAME_PREFIX + ":name=" + threadPoolStats.getPoolName());
                ThreadPoolStatsJMX stats = new ThreadPoolStatsJMX(threadPoolStats);
                server.registerMBean(stats, name);
            } catch (JMException e) {
                log.error("collect thread pool stats error", e);
            }
            GAUGE_CACHE.put(threadPoolStats.getPoolName(), threadPoolStats);
        }
    }

    @Override
    public void collect(VTExecutorStats vtExecutorStats) {
        if (GAUGE_CACHE.containsKey(vtExecutorStats.getExecutorName())) {
            ThreadPoolStats poolStats = (ThreadPoolStats) GAUGE_CACHE.get(vtExecutorStats.getExecutorName());
            BeanUtils.copyProperties(vtExecutorStats, poolStats);
        } else {
            try {
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                ObjectName name = new ObjectName(DTP_METRIC_NAME_PREFIX + ":name=" + vtExecutorStats.getExecutorName());
                VTExecutorStatsJMX stats = new VTExecutorStatsJMX(vtExecutorStats);
                server.registerMBean(stats, name);
            } catch (JMException e) {
                log.error("collect thread pool stats error", e);
            }
            GAUGE_CACHE.put(vtExecutorStats.getExecutorName(), vtExecutorStats);
        }
    }

    @Override
    public String type() {
        return CollectorTypeEnum.JMX.name().toLowerCase();
    }

}
