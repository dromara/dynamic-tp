package com.dtp.core.monitor.collector;

import com.dtp.core.DtpExecutor;

/**
 * MetricsCollector related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public interface MetricsCollector {

    /**
     * Collect key metrics.
     * @param executor dtpExecutor instance
     */
    void collect(DtpExecutor executor);

    /**
     * Collector type.
     * @return collector type
     */
    String type();

    /**
     * Judge collector type.
     * @param type collector type
     * @return true if the collector supports this type, else false
     */
    boolean support(String type);
}
