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

import com.google.common.collect.Maps;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.entity.TpExecutorProps;
import org.dromara.dynamictp.common.manager.ContextManagerHelper;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.StreamUtil;
import org.dromara.dynamictp.core.support.ExecutorWrapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * HystrixDtpAdapter related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
public class HystrixDtpAdapter extends AbstractDtpAdapter {

    private static final String TP_PREFIX = "hystrixTp";

    private static final String THREAD_POOL_FIELD = "threadPool";

    private static final Map<String, DtpMetricsPublisherThreadPool> METRICS_PUBLISHERS = Maps.newHashMap();

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(dtpProperties.getHystrixTp(), dtpProperties.getPlatforms());
    }

    @Override
    public void refresh(ExecutorWrapper executorWrapper, List<NotifyPlatform> platforms, TpExecutorProps props) {
        super.refresh(executorWrapper, platforms, props);
        val metricsPublisher = METRICS_PUBLISHERS.get(executorWrapper.getThreadPoolName());
        if (Objects.isNull(metricsPublisher)) {
            return;
        }
        metricsPublisher.refreshProperties(props);
    }

    @Override
    protected String getTpPrefix() {
        return TP_PREFIX;
    }

    public void register(String poolName, HystrixThreadPoolMetrics metrics) {
        ThreadPoolExecutor threadPoolExecutor = metrics.getThreadPool();
        if (executors.containsKey(poolName)) {
            return;
        }

        DtpProperties dtpProperties = ContextManagerHelper.getBean(DtpProperties.class);
        val prop = StreamUtil.toMap(dtpProperties.getHystrixTp(), TpExecutorProps::getThreadPoolName);
        String tpName = TP_PREFIX + "#" + poolName;
        enhanceOriginExecutor(tpName, threadPoolExecutor, THREAD_POOL_FIELD, metrics);
        refresh(executors.get(tpName), dtpProperties.getPlatforms(), prop.get(tpName));
        log.info("DynamicTp adapter, {} init end, executor {}", getTpPrefix(), executors.get(tpName));
    }

    public void cacheMetricsPublisher(String poolName, DtpMetricsPublisherThreadPool metricsPublisher) {
        METRICS_PUBLISHERS.putIfAbsent(poolName, metricsPublisher);
    }

    @Override
    protected void initialize() {
        super.initialize();
        HystrixEventNotifier eventNotifier = HystrixPlugins.getInstance().getEventNotifier();
        HystrixPropertiesStrategy propertiesStrategy = HystrixPlugins.getInstance().getPropertiesStrategy();
        HystrixCommandExecutionHook commandExecutionHook = HystrixPlugins.getInstance().getCommandExecutionHook();
        HystrixConcurrencyStrategy concurrencyStrategy = HystrixPlugins.getInstance().getConcurrencyStrategy();
        HystrixMetricsPublisher metricsPublisher = HystrixPlugins.getInstance().getMetricsPublisher();

        HystrixPlugins.reset();

        HystrixPlugins.getInstance().registerMetricsPublisher(new DtpHystrixMetricsPublisher(metricsPublisher));
        HystrixPlugins.getInstance().registerConcurrencyStrategy(concurrencyStrategy);
        HystrixPlugins.getInstance().registerEventNotifier(eventNotifier);
        HystrixPlugins.getInstance().registerPropertiesStrategy(propertiesStrategy);
        HystrixPlugins.getInstance().registerCommandExecutionHook(commandExecutionHook);
    }
}
