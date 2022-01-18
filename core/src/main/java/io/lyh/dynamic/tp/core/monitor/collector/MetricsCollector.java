package io.lyh.dynamic.tp.core.monitor.collector;

import io.lyh.dynamic.tp.core.DtpExecutor;

/**
 * MetricsCollector related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public interface MetricsCollector {

    /**
     * Collect key metrics.
     * @param executor DtpExecutor
     */
    void collect(DtpExecutor executor);

    /**
     * Collector type.
     * @return
     */
    String type();

    /**
     * Judge collector type.
     * @param type
     * @return
     */
    boolean support(String type);
}
