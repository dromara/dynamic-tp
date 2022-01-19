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
        Metrics.gauge("thread.pool.core.size", tags, metrics.getCorePoolSize());
        Metrics.gauge("thread.pool.maximum.size", tags, metrics.getMaximumPoolSize());
        Metrics.gauge("thread.pool.current.size", tags, metrics.getPoolSize());
        Metrics.gauge("thread.pool.largest.size", tags, metrics.getLargestPoolSize());
        Metrics.gauge("thread.pool.active.count", tags, metrics.getActiveCount());

        Metrics.gauge("thread.pool.task.count", tags, metrics.getTaskCount());
        Metrics.gauge("thread.pool.completed.task.count", tags, metrics.getCompletedTaskCount());
        Metrics.gauge("thread.pool.wait.task.count", tags, metrics.getWaitTaskCount());

        Metrics.gauge("thread.pool.queue.size", tags, metrics.getQueueSize());
        Metrics.gauge("thread.pool.queue.capacity", tags, metrics.getQueueCapacity());
        Metrics.gauge("thread.pool.queue.remaining.capacity", tags, metrics.getQueueRemainingCapacity());

        Metrics.gauge("thread.pool.reject.count", tags, metrics.getRejectCount());
    }
}
