package com.dtp.adapter.hystrix;

import com.dtp.adapter.common.AbstractDtpHandler;
import com.dtp.adapter.hystrix.handler.HystrixDtpHandler;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.SimpleTpProperties;
import com.dtp.common.util.ReflectionUtil;
import com.dtp.common.util.StreamUtil;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherThreadPool;
import com.netflix.hystrix.strategy.properties.HystrixProperty;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Objects;

import static com.netflix.hystrix.strategy.properties.HystrixPropertiesChainedProperty.forBoolean;
import static com.netflix.hystrix.strategy.properties.HystrixPropertiesChainedProperty.forInteger;

/**
 * DtpMetricsPublisherThreadPool related
 *
 * @author yanhom
 * @since 1.0.8
 */
@Slf4j
public class DtpMetricsPublisherThreadPool implements HystrixMetricsPublisherThreadPool {

    private static final String PROPERTY_PREFIX = "hystrix";
    private static final int DEFAULT_CORE_SIZE = 10;
    private static final int DEFAULT_MAXIMUM_SIZE = 10;
    private static final int DEFAULT_KEEP_ALIVE_TIME_MINUTES = 1;

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
        try {
            val corePoolSize = getProperty(threadPoolKey, "coreSize", simpleTpProperties.getCorePoolSize(), DEFAULT_CORE_SIZE);
            ReflectionUtil.setFieldValue(HystrixThreadPoolProperties.class, "corePoolSize", properties, corePoolSize);

            val maxPoolSize = getProperty(threadPoolKey, "maximumSize", simpleTpProperties.getMaximumPoolSize(), DEFAULT_MAXIMUM_SIZE);
            ReflectionUtil.setFieldValue(HystrixThreadPoolProperties.class, "maximumPoolSize", properties, maxPoolSize);

            val keepAliveTime = getProperty(threadPoolKey, "keepAliveTimeMinutes", simpleTpProperties.getKeepAliveTime() / 60, DEFAULT_KEEP_ALIVE_TIME_MINUTES);
            ReflectionUtil.setFieldValue(HystrixThreadPoolProperties.class, "keepAliveTime", properties, keepAliveTime);

            val allowMaximumSizeToDivergeFromCoreSize = getProperty(threadPoolKey, "allowMaximumSizeToDivergeFromCoreSize", true, true);
            ReflectionUtil.setFieldValue(HystrixThreadPoolProperties.class, "allowMaximumSizeToDivergeFromCoreSize", properties, allowMaximumSizeToDivergeFromCoreSize);
        } catch (IllegalAccessException e) {
            log.error("DynamicTp hystrix adapter, reset hystrix threadPool properties failed.", e);
        }
    }

    private static HystrixProperty<Integer> getProperty(HystrixThreadPoolKey key, String instanceProperty, Integer builderOverrideValue, Integer defaultValue) {
        return forInteger()
                .add(PROPERTY_PREFIX + ".threadpool." + key.name() + "." + instanceProperty, builderOverrideValue)
                .add(PROPERTY_PREFIX + ".threadpool.default." + instanceProperty, defaultValue)
                .build();
    }

    private static HystrixProperty<Boolean> getProperty(HystrixThreadPoolKey key, String instanceProperty, Boolean builderOverrideValue, Boolean defaultValue) {
        return forBoolean()
                .add(PROPERTY_PREFIX + ".threadpool." + key.name() + "." + instanceProperty, builderOverrideValue)
                .add(PROPERTY_PREFIX + ".threadpool.default." + instanceProperty, defaultValue)
                .build();
    }
}
