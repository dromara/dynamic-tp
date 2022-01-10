package io.lyh.dtp.monitor.collector;

import io.lyh.dtp.common.em.CollectorTypeEnum;
import io.lyh.dtp.core.DtpExecutor;

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
    CollectorTypeEnum type();

    /**
     * Judge collector type.
     * @param type
     * @return
     */
    boolean support(String type);
}
