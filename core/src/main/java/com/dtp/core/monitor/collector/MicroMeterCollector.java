package com.dtp.core.monitor.collector;

import com.dtp.common.dto.ThreadPoolMetrics;
import com.dtp.common.em.CollectorTypeEnum;
import com.dtp.core.DtpExecutor;
import com.dtp.core.helper.MetricsHelper;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

/**
 * MicroMeterCollector related
 *
 * @author: yanhom
 * @since 1.0.0
 */
@Slf4j
public class MicroMeterCollector extends AbstractCollector {

    @Override
    public void collect(DtpExecutor executor) {
        gauge(MetricsHelper.getMetrics(executor));
    }

    @Override
    public String type() {
        return CollectorTypeEnum.MICROMETER.name();
    }

    public void gauge(ThreadPoolMetrics metrics) {

        Iterable<Tag> tags = Collections.singletonList(Tag.of("thread.pool.name", metrics.getDtpName()));
        Metrics.gauge("thread.pool.core.size", tags, metrics, ThreadPoolMetrics::getCorePoolSize);
        Metrics.gauge("thread.pool.maximum.size", tags, metrics, ThreadPoolMetrics::getMaximumPoolSize);
        Metrics.gauge("thread.pool.current.size", tags, metrics, ThreadPoolMetrics::getPoolSize);
        Metrics.gauge("thread.pool.largest.size", tags, metrics, ThreadPoolMetrics::getLargestPoolSize);
        Metrics.gauge("thread.pool.active.count", tags, metrics, ThreadPoolMetrics::getActiveCount);

        Metrics.gauge("thread.pool.task.count", tags, metrics, ThreadPoolMetrics::getTaskCount);
        Metrics.gauge("thread.pool.completed.task.count", tags, metrics, ThreadPoolMetrics::getCompletedTaskCount);
        Metrics.gauge("thread.pool.wait.task.count", tags, metrics, ThreadPoolMetrics::getWaitTaskCount);

        Metrics.gauge("thread.pool.queue.size", tags, metrics, ThreadPoolMetrics::getQueueSize);
        Metrics.gauge("thread.pool.queue.capacity", tags, metrics, ThreadPoolMetrics::getQueueCapacity);
        Metrics.gauge("thread.pool.queue.remaining.capacity", tags, metrics, ThreadPoolMetrics::getQueueRemainingCapacity);

        Metrics.gauge("thread.pool.reject.count", tags, metrics, ThreadPoolMetrics::getRejectCount);
    }
}
