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

package org.dromara.dynamictp.test.core.monitor.collector.jmx;

import org.dromara.dynamictp.common.em.CollectorTypeEnum;
import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.core.monitor.collector.jmx.JMXCollector;
import org.dromara.dynamictp.core.monitor.collector.jmx.ThreadPoolStatsJMX;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

/**
 * JMXCollectorTest related.
 */
class JMXCollectorTest {

    @Test
    void testThreadPoolStatsJmxGetsAndSetsStats() {
        ThreadPoolStats first = new ThreadPoolStats();
        ThreadPoolStats second = new ThreadPoolStats();
        ThreadPoolStatsJMX statsJMX = new ThreadPoolStatsJMX(first);

        statsJMX.setThreadPoolStats(second);

        Assertions.assertSame(second, statsJMX.getThreadPoolStats());
    }

    @Test
    void testTypeReturnsJmxCollectorType() {
        JMXCollector collector = new JMXCollector();

        Assertions.assertEquals(CollectorTypeEnum.JMX.name().toLowerCase(), collector.type());
        Assertions.assertTrue(collector.support("JMX"));
        Assertions.assertFalse(collector.support("logging"));
    }

    @Test
    void testCollectReusesCachedStatsForSamePoolName() throws Exception {
        String poolName = "jmx-test-" + UUID.randomUUID();
        ObjectName objectName = new ObjectName(JMXCollector.DTP_METRIC_NAME_PREFIX + ":name=" + poolName);
        Map<String, ThreadPoolStats> gaugeCache = getGaugeCache();
        gaugeCache.remove(poolName);

        try {
            ThreadPoolStats first = stats(poolName, 1);
            ThreadPoolStats second = stats(poolName, 8);
            JMXCollector collector = new JMXCollector();

            collector.collect(first);
            collector.collect(second);

            Assertions.assertSame(first, gaugeCache.get(poolName));
            Assertions.assertEquals(8, first.getPoolSize());
        } finally {
            gaugeCache.remove(poolName);
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            if (server.isRegistered(objectName)) {
                server.unregisterMBean(objectName);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ThreadPoolStats> getGaugeCache() throws Exception {
        Field field = JMXCollector.class.getDeclaredField("GAUGE_CACHE");
        field.setAccessible(true);
        return (Map<String, ThreadPoolStats>) field.get(null);
    }

    private static ThreadPoolStats stats(String poolName, int poolSize) {
        ThreadPoolStats stats = new ThreadPoolStats();
        stats.setPoolName(poolName);
        stats.setPoolSize(poolSize);
        return stats;
    }
}
