package com.dtp.adapter.hystrix.handler;

import cn.hutool.core.collection.CollUtil;
import com.dtp.adapter.common.AbstractDtpHandler;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.SimpleTpProperties;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.util.StreamUtil;
import com.google.common.collect.Maps;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collection;
import java.util.Map;

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
    public Map<String, ExecutorWrapper> getExecutorWrappers() {
        if (CollUtil.isNotEmpty(HYSTRIX_EXECUTORS)) {
            return HYSTRIX_EXECUTORS;
        }

        Collection<HystrixThreadPoolMetrics> threadPoolMetrics = HystrixThreadPoolMetrics.getInstances();
        if (CollUtil.isEmpty(threadPoolMetrics)) {
            return HYSTRIX_EXECUTORS;
        }
        threadPoolMetrics.forEach(x -> {
            val threadPoolKey = x.getThreadPoolKey();
            val executorWrapper = new ExecutorWrapper(threadPoolKey.name(), x.getThreadPool());
            initNotifyItems(threadPoolKey.name(), executorWrapper);
            HYSTRIX_EXECUTORS.put(threadPoolKey.name(), executorWrapper);
        });

        log.info("DynamicTp adapter, hystrix executors init end, executors: {}", HYSTRIX_EXECUTORS);
        return HYSTRIX_EXECUTORS;
    }
}
