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

package org.dromara.dynamictp.core.monitor.collector;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.em.CollectorTypeEnum;
import org.dromara.dynamictp.common.entity.ExecutorStats;
import org.dromara.dynamictp.common.util.BeanCopierUtil;
import org.dromara.dynamictp.common.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MicroMeterCollector related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public class MicroMeterCollector extends AbstractCollector {

    /**
     * Prefix used for all dtp metric names.
     */
    public static final String DTP_METRIC_NAME_PREFIX = "thread.pool";

    public static final String POOL_NAME_TAG = DTP_METRIC_NAME_PREFIX + ".name";

    public static final String POOL_ALIAS_TAG = DTP_METRIC_NAME_PREFIX + ".alias";

    public static final String APP_NAME_TAG = "app.name";

    public static final Map<String, ExecutorStats> GAUGE_CACHE = new ConcurrentHashMap<>();

    @Override
    public void collect(ExecutorStats executorStats) {
        // metrics must be held with a strong reference, even though it is never referenced within this class
        ExecutorStats oldStats = GAUGE_CACHE.get(executorStats.getExecutorName());
        if (Objects.isNull(oldStats)) {
            GAUGE_CACHE.put(executorStats.getExecutorName(), executorStats);
        } else {
            BeanCopierUtil.copyProperties(executorStats, oldStats);
        }
        gauge(GAUGE_CACHE.get(executorStats.getExecutorName()));
    }

    @Override
    public String type() {
        return CollectorTypeEnum.MICROMETER.name().toLowerCase();
    }

    public void gauge(ExecutorStats executorStats) {

        Iterable<Tag> tags = getTags(executorStats);

        Metrics.gauge(metricName("core.size"), tags, executorStats, ExecutorStats::getCorePoolSize);
        Metrics.gauge(metricName("maximum.size"), tags, executorStats, ExecutorStats::getMaximumPoolSize);
        Metrics.gauge(metricName("current.size"), tags, executorStats, ExecutorStats::getPoolSize);
        Metrics.gauge(metricName("largest.size"), tags, executorStats, ExecutorStats::getLargestPoolSize);

        Metrics.gauge(metricName("completed.task.count"), tags, executorStats, ExecutorStats::getCompletedTaskCount);
        Metrics.gauge(metricName("wait.task.count"), tags, executorStats, ExecutorStats::getWaitTaskCount);

        Metrics.gauge(metricName("queue.size"), tags, executorStats, ExecutorStats::getQueueSize);
        Metrics.gauge(metricName("queue.capacity"), tags, executorStats, ExecutorStats::getQueueCapacity);
        Metrics.gauge(metricName("queue.remaining.capacity"), tags, executorStats, ExecutorStats::getQueueRemainingCapacity);

        Metrics.gauge(metricName("reject.count"), tags, executorStats, ExecutorStats::getRejectCount);
        Metrics.gauge(metricName("queue.timeout.count"), tags, executorStats, ExecutorStats::getQueueTimeoutCount);

        Metrics.gauge(metricName("active.count"), tags, executorStats, ExecutorStats::getActiveCount);
        Metrics.gauge(metricName("task.count"), tags, executorStats, ExecutorStats::getTaskCount);
        Metrics.gauge(metricName("run.timeout.count"), tags, executorStats, ExecutorStats::getRunTimeoutCount);

        Metrics.gauge(metricName("tps"), tags, executorStats, ExecutorStats::getTps);
        Metrics.gauge(metricName("completed.task.time.avg"), tags, executorStats, ExecutorStats::getAvg);
        Metrics.gauge(metricName("completed.task.time.max"), tags, executorStats, ExecutorStats::getMaxRt);
        Metrics.gauge(metricName("completed.task.time.min"), tags, executorStats, ExecutorStats::getMinRt);
        Metrics.gauge(metricName("completed.task.time.tp50"), tags, executorStats, ExecutorStats::getTp50);
        Metrics.gauge(metricName("completed.task.time.tp75"), tags, executorStats, ExecutorStats::getTp75);
        Metrics.gauge(metricName("completed.task.time.tp90"), tags, executorStats, ExecutorStats::getTp90);
        Metrics.gauge(metricName("completed.task.time.tp95"), tags, executorStats, ExecutorStats::getTp95);
        Metrics.gauge(metricName("completed.task.time.tp99"), tags, executorStats, ExecutorStats::getTp99);
        Metrics.gauge(metricName("completed.task.time.tp999"), tags, executorStats, ExecutorStats::getTp999);
    }

    private static String metricName(String name) {
        return String.join(".", DTP_METRIC_NAME_PREFIX, name);
    }

    private Iterable<Tag> getTags(ExecutorStats executorStats) {
        List<Tag> tags = new ArrayList<>(3);
        tags.add(Tag.of(POOL_NAME_TAG, executorStats.getExecutorName()));
        tags.add(Tag.of(APP_NAME_TAG, CommonUtil.getInstance().getServiceName()));
        tags.add(Tag.of(POOL_ALIAS_TAG, Optional.ofNullable(executorStats.getExecutorAliasName()).orElse(executorStats.getExecutorName())));
        return tags;
    }
}

