package com.dtp.adapter.hystrix;

import com.dtp.adapter.hystrix.handler.HystrixDtpHandler;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.SimpleTpProperties;
import com.dtp.common.util.ReflectionUtil;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherThreadPool;
import com.netflix.hystrix.strategy.properties.HystrixProperty;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private final AtomicBoolean init = new AtomicBoolean(false);

    private final HystrixThreadPoolKey threadPoolKey;
    private final HystrixThreadPoolMetrics metrics;
    private final HystrixThreadPoolProperties threadPoolProperties;
    private final HystrixMetricsPublisherThreadPool metricsPublisherForThreadPool;

    public DtpMetricsPublisherThreadPool(
            final HystrixThreadPoolKey threadPoolKey,
            final HystrixThreadPoolMetrics metrics,
            final HystrixThreadPoolProperties threadPoolProperties,
            final HystrixMetricsPublisherThreadPool metricsPublisherForThreadPool) {
        this.threadPoolKey = threadPoolKey;
        this.metrics = metrics;
        this.threadPoolProperties = threadPoolProperties;
        this.metricsPublisherForThreadPool = metricsPublisherForThreadPool;
    }

    @Override
    public void initialize() {
        metricsPublisherForThreadPool.initialize();
        HystrixDtpHandler hystrixTpHandler = ApplicationContextHolder.getBean(HystrixDtpHandler.class);
        hystrixTpHandler.cacheMetricsPublisher(threadPoolKey.name(), this);
        hystrixTpHandler.register(threadPoolKey.name(), metrics.getThreadPool());
    }

    public void refreshProperties(SimpleTpProperties properties) {
        if (Objects.isNull(properties)) {
            return;
        }

        try {
            if (!Objects.equals(threadPoolProperties.coreSize().get(), properties.getCorePoolSize())) {
                val corePoolSize = getProperty(threadPoolKey, "coreSize",
                        properties.getCorePoolSize(), DEFAULT_CORE_SIZE);
                ReflectionUtil.setFieldValue(HystrixThreadPoolProperties.class,
                        "corePoolSize", threadPoolProperties, corePoolSize);
            }

            if (!Objects.equals(threadPoolProperties.maximumSize().get(), properties.getMaximumPoolSize())) {
                val maxPoolSize = getProperty(threadPoolKey, "maximumSize",
                        properties.getMaximumPoolSize(), DEFAULT_MAXIMUM_SIZE);
                ReflectionUtil.setFieldValue(HystrixThreadPoolProperties.class,
                        "maximumPoolSize", threadPoolProperties, maxPoolSize);
            }

            val keepAliveTimeMinutes = (int) TimeUnit.SECONDS.toMinutes(properties.getKeepAliveTime());
            if (!Objects.equals(threadPoolProperties.keepAliveTimeMinutes().get(), keepAliveTimeMinutes)) {
                val keepAliveTimeProperty = getProperty(threadPoolKey,
                        "keepAliveTimeMinutes", keepAliveTimeMinutes, DEFAULT_KEEP_ALIVE_TIME_MINUTES);
                ReflectionUtil.setFieldValue(HystrixThreadPoolProperties.class,
                        "keepAliveTime", threadPoolProperties, keepAliveTimeProperty);
            }

            if (init.compareAndSet(false, true)) {
                val allowSetMax = getProperty(threadPoolKey,
                        "allowMaximumSizeToDivergeFromCoreSize", true, true);
                ReflectionUtil.setFieldValue(HystrixThreadPoolProperties.class,
                        "allowMaximumSizeToDivergeFromCoreSize", threadPoolProperties, allowSetMax);
            }
        } catch (IllegalAccessException e) {
            log.error("DynamicTp hystrix adapter, reset hystrix threadPool properties failed.", e);
        }
    }

    private static HystrixProperty<Integer> getProperty(HystrixThreadPoolKey key,
                                                        String instanceProperty,
                                                        Integer builderOverrideValue,
                                                        Integer defaultValue) {
        return forInteger()
                .add(PROPERTY_PREFIX + ".threadpool." + key.name() + "." + instanceProperty, builderOverrideValue)
                .add(PROPERTY_PREFIX + ".threadpool.default." + instanceProperty, defaultValue)
                .build();
    }

    private static HystrixProperty<Boolean> getProperty(HystrixThreadPoolKey key,
                                                        String instanceProperty,
                                                        Boolean builderOverrideValue,
                                                        Boolean defaultValue) {
        return forBoolean()
                .add(PROPERTY_PREFIX + ".threadpool." + key.name() + "." + instanceProperty, builderOverrideValue)
                .add(PROPERTY_PREFIX + ".threadpool.default." + instanceProperty, defaultValue)
                .build();
    }
}
