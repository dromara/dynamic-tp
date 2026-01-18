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

package org.dromara.dynamictp.client.monitor;

import org.dromara.dynamictp.common.em.CollectorTypeEnum;
import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.core.monitor.collector.AbstractCollector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * StatsCollector for collecting thread pool statistics
 * Enhanced to support active collection and multiple thread pools
 *
 * @author eachann
 */
public class StatsCollector extends AbstractCollector {

    private static final Map<String, ThreadPoolStats> MULTI_POOL_STATS = new ConcurrentHashMap<>();

    /**
     * Get statistics for all thread pools
     *
     * @return list of thread pool statistics
     */
    public static List<ThreadPoolStats> getMultiPoolStats() {
        return new ArrayList<>(MULTI_POOL_STATS.values());
    }

    /**
     * Get statistics for a specific thread pool
     *
     * @param poolName the name of the thread pool
     * @return the statistics for the thread pool, or null if not found
     */
    public static ThreadPoolStats getPoolStats(String poolName) {
        return MULTI_POOL_STATS.get(poolName);
    }

    /**
     * Get all registered pool names
     *
     * @return set of pool names
     */
    public static Set<String> getPoolNames() {
        return Collections.unmodifiableSet(MULTI_POOL_STATS.keySet());
    }

    /**
     * Remove statistics for a specific thread pool
     *
     * @param poolName the name of the thread pool to remove
     */
    public static void removePoolStats(String poolName) {
        MULTI_POOL_STATS.remove(poolName);
    }

    /**
     * Clear all collected statistics
     * This should be called when the collector is no longer needed
     */
    public static void clear() {
        MULTI_POOL_STATS.clear();
    }

    /**
     * Get the number of monitored thread pools
     *
     * @return the count of thread pools
     */
    public static int size() {
        return MULTI_POOL_STATS.size();
    }

    @Override
    public void collect(ThreadPoolStats poolStats) {
        if (poolStats != null && poolStats.getPoolName() != null) {
            MULTI_POOL_STATS.put(poolStats.getPoolName(), poolStats);
        }
    }

    @Override
    public String type() {
        return CollectorTypeEnum.ADMIN.name().toLowerCase();
    }
}
