package com.dtp.adapter.hystrix;

import com.netflix.hystrix.*;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherCollapser;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherCommand;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherThreadPool;
import io.micrometer.core.instrument.binder.hystrix.MicrometerMetricsPublisherCommand;

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
    public HystrixMetricsPublisherThreadPool getMetricsPublisherForThreadPool(HystrixThreadPoolKey threadPoolKey, HystrixThreadPoolMetrics metrics, HystrixThreadPoolProperties properties) {
        HystrixMetricsPublisherThreadPool metricsPublisherForThreadPool =
                metricsPublisher.getMetricsPublisherForThreadPool(threadPoolKey, metrics, properties);
        return new DtpMetricsPublisherThreadPool(threadPoolKey, metrics, properties, metricsPublisherForThreadPool);
    }

    @Override
    public HystrixMetricsPublisherCollapser getMetricsPublisherForCollapser(HystrixCollapserKey collapserKey, HystrixCollapserMetrics metrics, HystrixCollapserProperties properties) {
        return metricsPublisher.getMetricsPublisherForCollapser(collapserKey, metrics, properties);
    }

    @Override
    public HystrixMetricsPublisherCommand getMetricsPublisherForCommand(HystrixCommandKey commandKey,
                                                                        HystrixCommandGroupKey commandGroupKey,
                                                                        HystrixCommandMetrics metrics,
                                                                        HystrixCircuitBreaker circuitBreaker,
                                                                        HystrixCommandProperties properties) {
        HystrixMetricsPublisherCommand metricsPublisherForCommand =
                metricsPublisher.getMetricsPublisherForCommand(commandKey, commandGroupKey, metrics, circuitBreaker, properties);
        return metricsPublisherForCommand;
    }
}

