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

package org.dromara.dynamictp.sdk.client.handler.collector;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.converter.ExecutorConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * AdminCollector for collecting thread pool statistics
 * Enhanced to support active collection and multiple thread pools
 *
 * @author eachann
 */
@Slf4j
public class AdminCollector {

    @Getter
    private ThreadPoolStats poolStats = new ThreadPoolStats();

    @Getter
    private final List<ThreadPoolStats> multiPoolStats = new CopyOnWriteArrayList<>();

    /**
     * Actively collect all thread pool statistics from DtpRegistry
     * This method can be called independently to refresh data
     */
    public void collectAllPoolStats() {
        try {
            Set<String> executorNames = DtpRegistry.getAllExecutorNames();
            if (executorNames.isEmpty()) {
                log.debug("No executors found in DtpRegistry");
                return;
            }

            List<ThreadPoolStats> newStatsList = new ArrayList<>();
            for (String executorName : executorNames) {
                try {
                    ThreadPoolStats stats = ExecutorConverter.toMetrics(DtpRegistry.getExecutorWrapper(executorName));
                    if (stats != null) {
                        newStatsList.add(stats);
                    }
                } catch (Exception e) {
                    log.warn("Failed to collect stats for executor: {}", executorName, e);
                }
            }

            // Update multiPoolStats
            this.multiPoolStats.clear();
            this.multiPoolStats.addAll(newStatsList);

            // Set the first pool stats as the main poolStats for backward compatibility
            if (!newStatsList.isEmpty()) {
                this.poolStats = newStatsList.get(0);
            }

            log.debug("AdminCollector actively collected {} pool stats", newStatsList.size());
        } catch (Exception e) {
            log.error("Failed to collect all pool stats", e);
        }
    }
}
