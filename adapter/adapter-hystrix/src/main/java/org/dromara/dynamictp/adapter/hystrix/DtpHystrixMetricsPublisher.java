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

