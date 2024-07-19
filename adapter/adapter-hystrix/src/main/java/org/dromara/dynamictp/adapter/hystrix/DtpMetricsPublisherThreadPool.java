/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.adapter.hystrix;

import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherThreadPool;
import com.netflix.hystrix.strategy.properties.HystrixProperty;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.dromara.dynamictp.common.entity.TpExecutorProps;
import org.dromara.dynamictp.common.manager.ContextManagerHelper;
import org.dromara.dynamictp.common.util.ReflectionUtil;

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
        HystrixDtpAdapter hystrixTpHandler = ContextManagerHelper.getBean(HystrixDtpAdapter.class);
        hystrixTpHandler.cacheMetricsPublisher(threadPoolKey.name(), this);
        hystrixTpHandler.register(threadPoolKey.name(), metrics);
    }

    public void refreshProperties(TpExecutorProps props) {
        if (Objects.isNull(props)) {
            return;
        }

        if (!Objects.equals(threadPoolProperties.coreSize().get(), props.getCorePoolSize())) {
            val corePoolSize = getProperty(threadPoolKey, "coreSize",
                    props.getCorePoolSize(), DEFAULT_CORE_SIZE);
            ReflectionUtil.setFieldValue(HystrixThreadPoolProperties.class,
                    "corePoolSize", threadPoolProperties, corePoolSize);
        }

        if (!Objects.equals(threadPoolProperties.maximumSize().get(), props.getMaximumPoolSize())) {
            val maxPoolSize = getProperty(threadPoolKey, "maximumSize",
                    props.getMaximumPoolSize(), DEFAULT_MAXIMUM_SIZE);
            ReflectionUtil.setFieldValue(HystrixThreadPoolProperties.class,
                    "maximumPoolSize", threadPoolProperties, maxPoolSize);
        }

        val keepAliveTimeMinutes = (int) TimeUnit.SECONDS.toMinutes(props.getKeepAliveTime());
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
    }

    private static HystrixProperty<Integer> getProperty(HystrixThreadPoolKey key,
                                                        String instanceProperty,
                                                        Integer builderOverrideValue,
                                                        Integer defaultValue) {
        return forInteger()
                .add(getPropertyName(key.name(), instanceProperty), builderOverrideValue)
                .add(getDefaultPropertyName(instanceProperty), defaultValue)
                .build();
    }

    private static HystrixProperty<Boolean> getProperty(HystrixThreadPoolKey key,
                                                        String instanceProperty,
                                                        Boolean builderOverrideValue,
                                                        Boolean defaultValue) {
        return forBoolean()
                .add(getPropertyName(key.name(), instanceProperty), builderOverrideValue)
                .add(getDefaultPropertyName(instanceProperty), defaultValue)
                .build();
    }

    private static String getPropertyName(String key, String instanceProperty) {
        return PROPERTY_PREFIX + ".threadpool." + key + "." + instanceProperty;
    }

    private static String getDefaultPropertyName(String instanceProperty) {
        return PROPERTY_PREFIX + ".threadpool.default." + instanceProperty;
    }
}
