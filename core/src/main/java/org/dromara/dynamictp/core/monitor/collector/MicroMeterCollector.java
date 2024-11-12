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
import org.dromara.dynamictp.common.entity.ThreadPoolStats;
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

    private static final Map<String, ThreadPoolStats> GAUGE_CACHE = new ConcurrentHashMap<>();

    @Override
    public void collect(ThreadPoolStats threadPoolStats) {
        // metrics must be held with a strong reference, even though it is never referenced within this class
        ThreadPoolStats oldStats = GAUGE_CACHE.get(threadPoolStats.getPoolName());
        if (Objects.isNull(oldStats)) {
            GAUGE_CACHE.put(threadPoolStats.getPoolName(), threadPoolStats);
        } else {
            BeanCopierUtil.copyProperties(threadPoolStats, oldStats);
        }
        gauge(GAUGE_CACHE.get(threadPoolStats.getPoolName()));
    }

    @Override
    public String type() {
        return CollectorTypeEnum.MICROMETER.name().toLowerCase();
    }

    public void gauge(ThreadPoolStats poolStats) {

        Iterable<Tag> tags = getTags(poolStats);

        Metrics.gauge(metricName("core.size"), tags, poolStats, ThreadPoolStats::getCorePoolSize);
        Metrics.gauge(metricName("maximum.size"), tags, poolStats, ThreadPoolStats::getMaximumPoolSize);
        Metrics.gauge(metricName("current.size"), tags, poolStats, ThreadPoolStats::getPoolSize);
        Metrics.gauge(metricName("largest.size"), tags, poolStats, ThreadPoolStats::getLargestPoolSize);
        Metrics.gauge(metricName("active.count"), tags, poolStats, ThreadPoolStats::getActiveCount);

        Metrics.gauge(metricName("task.count"), tags, poolStats, ThreadPoolStats::getTaskCount);
        Metrics.gauge(metricName("completed.task.count"), tags, poolStats, ThreadPoolStats::getCompletedTaskCount);
        Metrics.gauge(metricName("wait.task.count"), tags, poolStats, ThreadPoolStats::getWaitTaskCount);

        Metrics.gauge(metricName("queue.size"), tags, poolStats, ThreadPoolStats::getQueueSize);
        Metrics.gauge(metricName("queue.capacity"), tags, poolStats, ThreadPoolStats::getQueueCapacity);
        Metrics.gauge(metricName("queue.remaining.capacity"), tags, poolStats, ThreadPoolStats::getQueueRemainingCapacity);

        Metrics.gauge(metricName("reject.count"), tags, poolStats, ThreadPoolStats::getRejectCount);
        Metrics.gauge(metricName("run.timeout.count"), tags, poolStats, ThreadPoolStats::getRunTimeoutCount);
        Metrics.gauge(metricName("queue.timeout.count"), tags, poolStats, ThreadPoolStats::getQueueTimeoutCount);

        Metrics.gauge(metricName("tps"), tags, poolStats, ThreadPoolStats::getTps);
        Metrics.gauge(metricName("completed.task.time.avg"), tags, poolStats, ThreadPoolStats::getAvg);
        Metrics.gauge(metricName("completed.task.time.max"), tags, poolStats, ThreadPoolStats::getMaxRt);
        Metrics.gauge(metricName("completed.task.time.min"), tags, poolStats, ThreadPoolStats::getMinRt);
        Metrics.gauge(metricName("completed.task.time.tp50"), tags, poolStats, ThreadPoolStats::getTp50);
        Metrics.gauge(metricName("completed.task.time.tp75"), tags, poolStats, ThreadPoolStats::getTp75);
        Metrics.gauge(metricName("completed.task.time.tp90"), tags, poolStats, ThreadPoolStats::getTp90);
        Metrics.gauge(metricName("completed.task.time.tp95"), tags, poolStats, ThreadPoolStats::getTp95);
        Metrics.gauge(metricName("completed.task.time.tp99"), tags, poolStats, ThreadPoolStats::getTp99);
        Metrics.gauge(metricName("completed.task.time.tp999"), tags, poolStats, ThreadPoolStats::getTp999);
    }

    private static String metricName(String name) {
        return String.join(".", DTP_METRIC_NAME_PREFIX, name);
    }

    private Iterable<Tag> getTags(ThreadPoolStats poolStats) {
        List<Tag> tags = new ArrayList<>(3);
        tags.add(Tag.of(POOL_NAME_TAG, poolStats.getPoolName()));
        tags.add(Tag.of(APP_NAME_TAG, CommonUtil.getInstance().getServiceName()));
        // https://github.com/dromara/dynamic-tp/issues/359
        tags.add(Tag.of(POOL_ALIAS_TAG, Optional.ofNullable(poolStats.getPoolAliasName()).orElse(poolStats.getPoolName())));
        return tags;
    }
}

