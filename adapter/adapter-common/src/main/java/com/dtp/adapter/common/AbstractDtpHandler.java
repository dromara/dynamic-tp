package com.dtp.adapter.common;

import cn.hutool.core.collection.CollUtil;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.SimpleTpProperties;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.dto.ThreadPoolStats;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

import static com.dtp.common.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;

/**
 * AbstractDtpHandler related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
public abstract class AbstractDtpHandler implements DtpHandler, ApplicationListener<ApplicationStartedEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        try {
            DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
            refresh(dtpProperties);
        } catch (Exception e) {
            log.error("Init third party thread pool failed.", e);
        }
    }

    /**
     * Get multi thread pool stats.
     *
     * @return thead pools stats
     */
    @Override
    public List<ThreadPoolStats> getMultiPoolStats() {
        val executorWrappers = getExecutorWrappers();
        if (CollUtil.isEmpty(executorWrappers)) {
            return Collections.emptyList();
        }

        List<ThreadPoolStats> threadPoolStats = Lists.newArrayList();
        executorWrappers.forEach((k, v) -> {
            val e = (ThreadPoolExecutor) v.getExecutor();
            val stats = ThreadPoolStats.builder()
                    .corePoolSize(e.getCorePoolSize())
                    .maximumPoolSize(e.getMaximumPoolSize())
                    .queueType(e.getQueue().getClass().getSimpleName())
                    .queueCapacity(e.getQueue().size() + e.getQueue().remainingCapacity())
                    .queueSize(e.getQueue().size())
                    .queueRemainingCapacity(e.getQueue().remainingCapacity())
                    .activeCount(e.getActiveCount())
                    .taskCount(e.getTaskCount())
                    .completedTaskCount(e.getCompletedTaskCount())
                    .largestPoolSize(e.getLargestPoolSize())
                    .poolSize(e.getPoolSize())
                    .waitTaskCount(e.getQueue().size())
                    .poolName(k)
                    .build();
            threadPoolStats.add(stats);
        });
        return threadPoolStats;
    }

    public void updateBase(String name, SimpleTpProperties properties, ExecutorWrapper executorWrapper) {
        if (Objects.isNull(properties)) {
            return;
        }

        checkParams(properties);
        val executor = (ThreadPoolExecutor) executorWrapper.getExecutor();
        int oldCoreSize = executor.getCorePoolSize();
        int oldMaxSize = executor.getMaximumPoolSize();
        long oldKeepAlive = executor.getKeepAliveTime(properties.getUnit());

        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaximumPoolSize(properties.getMaximumPoolSize());
        executor.setKeepAliveTime(properties.getKeepAliveTime(), properties.getUnit());

        log.info("DynamicTp [{}] refreshed end, coreSize: [{}], maxSize: [{}], keepAliveTime: [{}]", name,
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldCoreSize, properties.getCorePoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldMaxSize, properties.getMaximumPoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldKeepAlive, properties.getKeepAliveTime()));
    }
}
