package com.dtp.adapter.hystrix;

import com.dtp.adapter.common.AbstractDtpHandler;
import com.dtp.adapter.hystrix.handler.HystrixDtpHandler;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.SimpleTpProperties;
import com.dtp.common.util.StreamUtil;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherThreadPool;
import lombok.val;

import java.util.Objects;

/**
 * DtpMetricsPublisherThreadPool related
 *
 * @author yanhom
 * @since 1.0.8
 */
public class DtpMetricsPublisherThreadPool implements HystrixMetricsPublisherThreadPool {

    private final HystrixThreadPoolKey threadPoolKey;
    private final HystrixThreadPoolMetrics metrics;

    private final HystrixThreadPoolProperties properties;
    private final HystrixMetricsPublisherThreadPool metricsPublisherForThreadPool;

    public DtpMetricsPublisherThreadPool(
            final HystrixThreadPoolKey threadPoolKey,
            final HystrixThreadPoolMetrics metrics,
            final HystrixThreadPoolProperties properties,
            final HystrixMetricsPublisherThreadPool metricsPublisherForThreadPool) {
        this.threadPoolKey = threadPoolKey;
        this.metrics = metrics;
        this.properties = properties;
        this.metricsPublisherForThreadPool = metricsPublisherForThreadPool;

    }

    @Override
    public void initialize() {
        metricsPublisherForThreadPool.initialize();
        AbstractDtpHandler hystrixTpHandler = ApplicationContextHolder.getBean(HystrixDtpHandler.class);
        hystrixTpHandler.register(threadPoolKey.name(), metrics.getThreadPool());

        DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
        val hystrixTp = dtpProperties.getHystrixTp();
        val tmpMap = StreamUtil.toMap(hystrixTp, SimpleTpProperties::getThreadPoolName);

        val simpleTpProperties = tmpMap.get(threadPoolKey.name());
        if (Objects.isNull(simpleTpProperties)) {
            return;
        }

        HystrixThreadPoolProperties.defaultSetter()
                .withCoreSize(simpleTpProperties.getCorePoolSize())
                .withMaximumSize(simpleTpProperties.getMaximumPoolSize())
                .withKeepAliveTimeMinutes(simpleTpProperties.getKeepAliveTime() / 60);
    }
}
