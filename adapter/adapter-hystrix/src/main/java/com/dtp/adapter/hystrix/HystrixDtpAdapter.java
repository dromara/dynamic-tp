package com.dtp.adapter.hystrix;

import com.dtp.adapter.common.AbstractDtpAdapter;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.entity.NotifyPlatform;
import com.dtp.common.entity.TpExecutorProps;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.util.StreamUtil;
import com.dtp.core.support.ExecutorWrapper;
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
 * HystrixDtpAdapter related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
public class HystrixDtpAdapter extends AbstractDtpAdapter {

    private static final String NAME = "hystrixTp";

    private static final Map<String, DtpMetricsPublisherThreadPool> METRICS_PUBLISHERS = Maps.newHashMap();

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getHystrixTp(), dtpProperties.getPlatforms());
    }

    @Override
    public void refresh(String name,
                        ExecutorWrapper executorWrapper,
                        List<NotifyPlatform> platforms,
                        TpExecutorProps props) {
        super.refresh(name, executorWrapper, platforms, props);
        val metricsPublisher = METRICS_PUBLISHERS.get(executorWrapper.getThreadPoolName());
        if (Objects.isNull(metricsPublisher)) {
            return;
        }
        metricsPublisher.refreshProperties(props);
    }

    @Override
    public void register(String poolName, ThreadPoolExecutor threadPoolExecutor) {
        if (executors.containsKey(poolName)) {
            return;
        }
        val executorWrapper = new ExecutorWrapper(poolName, threadPoolExecutor);
        initNotifyItems(poolName, executorWrapper);
        executors.put(poolName, executorWrapper);

        DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
        val tmpMap = StreamUtil.toMap(dtpProperties.getHystrixTp(), TpExecutorProps::getThreadPoolName);
        log.info("DynamicTp adapter, hystrix init end, executor {}", executorWrapper);
        refresh(NAME, executorWrapper, dtpProperties.getPlatforms(), tmpMap.get(poolName));
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
