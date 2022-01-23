package com.dtp.core.monitor.collector;

import cn.hutool.core.bean.BeanUtil;
import com.dtp.common.dto.ThreadPoolStats;
import com.dtp.common.em.CollectorTypeEnum;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MicroMeterCollector related
 *
 * @author: yanhom
 * @since 1.0.0
 */
@Slf4j
public class MicroMeterCollector extends AbstractCollector {

    /**
     * Prefix used for all dtp metric names.
     */
    public static final String DTP_METRIC_NAME_PREFIX = "thread.pool";

    public static final String TAG_KEY = DTP_METRIC_NAME_PREFIX + ".name";

    private static final Map<String, ThreadPoolStats> GAUGE_CACHE = new ConcurrentHashMap<>();

    @Override
    public void collect(ThreadPoolStats threadPoolStats) {
        // metrics must be held with a strong reference, even though it is never referenced within this class
        ThreadPoolStats oldStats = GAUGE_CACHE.get(threadPoolStats.getDtpName());
        if (Objects.isNull(oldStats)) {
            GAUGE_CACHE.put(threadPoolStats.getDtpName(), threadPoolStats);
        } else {
            BeanUtil.copyProperties(threadPoolStats, oldStats);
        }
        gauge(GAUGE_CACHE.get(threadPoolStats.getDtpName()));
    }

    @Override
    public String type() {
        return CollectorTypeEnum.MICROMETER.name();
    }

    public void gauge(ThreadPoolStats poolStats) {

        Iterable<Tag> tags = Collections.singletonList(Tag.of(TAG_KEY, poolStats.getDtpName()));

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
    }

    private static String metricName(String name) {
        return String.join(".", DTP_METRIC_NAME_PREFIX, name);
    }
}

