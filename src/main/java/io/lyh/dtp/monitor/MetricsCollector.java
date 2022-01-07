package io.lyh.dtp.monitor;

import io.lyh.dtp.core.DtpExecutor;

/**
 * MetricsCollector related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2022-01-05 18:11
 * @since 1.0.0
 **/
public interface MetricsCollector {

    /**
     * Collect key metrics.
     * @param executor DtpExecutor
     */
    void collect(DtpExecutor executor);
}
