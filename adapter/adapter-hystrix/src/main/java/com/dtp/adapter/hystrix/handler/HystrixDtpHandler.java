package com.dtp.adapter.hystrix.handler;

import cn.hutool.core.collection.CollUtil;
import com.dtp.adapter.common.AbstractDtpHandler;
import com.dtp.adapter.hystrix.DtpHystrixMetricsPublisher;
import com.dtp.adapter.hystrix.DtpMetricsPublisherThreadPool;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.SimpleTpProperties;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.dto.NotifyPlatform;
import com.dtp.common.util.StreamUtil;
import com.google.common.collect.Maps;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * HystrixDtpHandler related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
public class HystrixDtpHandler extends AbstractDtpHandler {

    private static final String NAME = "hystrixTp";

    private static final Map<String, ExecutorWrapper> HYSTRIX_EXECUTORS = Maps.newHashMap();

    private static final Map<String, DtpMetricsPublisherThreadPool> METRICS_PUBLISHERS = Maps.newHashMap();

    @Override
    public void refresh(DtpProperties dtpProperties) {
        val properties = dtpProperties.getHystrixTp();
        val executorWrappers = getExecutorWrappers();
        if (CollUtil.isEmpty(properties) || CollUtil.isEmpty(executorWrappers)) {
            return;
        }

        val tmpMap = StreamUtil.toMap(properties, SimpleTpProperties::getThreadPoolName);
        executorWrappers.forEach((k ,v) -> refresh(NAME, v, dtpProperties.getPlatforms(), tmpMap.get(k)));
    }

    @Override
    public void refresh(String name,
                        ExecutorWrapper executorWrapper,
                        List<NotifyPlatform> platforms,
                        SimpleTpProperties properties) {
        super.refresh(name, executorWrapper, platforms, properties);
        val metricsPublisher = METRICS_PUBLISHERS.get(executorWrapper.getThreadPoolName());
        if (Objects.isNull(metricsPublisher)) {
            return;
        }
        metricsPublisher.refreshProperties(properties);
    }

    @Override
    public Map<String, ExecutorWrapper> getExecutorWrappers() {
        return HYSTRIX_EXECUTORS;
    }

    @Override
    public void register(String poolName, ThreadPoolExecutor threadPoolExecutor) {
        if (HYSTRIX_EXECUTORS.containsKey(poolName)) {
            return;
        }
        val executorWrapper = new ExecutorWrapper(poolName, threadPoolExecutor);
        initNotifyItems(poolName, executorWrapper);
        HYSTRIX_EXECUTORS.put(poolName, executorWrapper);

        DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
        val properties = dtpProperties.getHystrixTp();
        val tmpMap = StreamUtil.toMap(properties, SimpleTpProperties::getThreadPoolName);

        refresh(NAME, executorWrapper, dtpProperties.getPlatforms(), tmpMap.get(poolName));
        log.info("DynamicTp adapter, hystrix executor [{}] init end", poolName);
    }

    public void cacheMetricsPublisher(String poolName, DtpMetricsPublisherThreadPool metricsPublisher) {
        METRICS_PUBLISHERS.putIfAbsent(poolName, metricsPublisher);
    }

    @Override
    protected void initialize() {
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

        super.initialize();
    }
}
