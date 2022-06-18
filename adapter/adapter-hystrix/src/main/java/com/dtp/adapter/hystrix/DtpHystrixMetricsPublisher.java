package com.dtp.adapter.hystrix;

import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherThreadPool;
import lombok.val;

/**
 * DtpHystrixMetricsPublisher related
 *
 * @author yanhom
 * @since 1.0.8
 */
public class DtpHystrixMetricsPublisher extends HystrixMetricsPublisher {
    private final HystrixMetricsPublisher metricsPublisher;

    public DtpHystrixMetricsPublisher(HystrixMetricsPublisher metricsPublisher) {
        this.metricsPublisher = metricsPublisher;
    }

    @Override
    public HystrixMetricsPublisherThreadPool getMetricsPublisherForThreadPool(HystrixThreadPoolKey threadPoolKey,
                                                                              HystrixThreadPoolMetrics metrics,
                                                                              HystrixThreadPoolProperties properties) {
        val metricsPublisherForThreadPool =
                metricsPublisher.getMetricsPublisherForThreadPool(threadPoolKey, metrics, properties);
        return new DtpMetricsPublisherThreadPool(threadPoolKey, metrics, properties, metricsPublisherForThreadPool);
    }
}

