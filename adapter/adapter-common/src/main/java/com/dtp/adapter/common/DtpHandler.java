package com.dtp.adapter.common;

import cn.hutool.core.map.MapUtil;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.SimpleTpProperties;
import com.dtp.common.dto.ThreadPoolStats;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * DtpHandler related
 *
 * @author yanhom
 * @since 1.0.6
 */
public interface DtpHandler {

    /**
     * Get specify thread pool executor.
     *
     * @return the specify executor
     */
    default Executor getExecutor() {
        return null;
    }

    /**
     * Get multi thread pool executors.
     *
     * @return executors
     */
    default Map<String, ? extends Executor> getExecutors() {
        return MapUtil.empty();
    }

    /**
     * Update thread pool with specify properties.
     *
     * @param dtpProperties the targeted dtpProperties
     */
    void updateTp(DtpProperties dtpProperties);

    /**
     * Get thread pool stats.
     *
     * @return the thread pool stats
     */
    default ThreadPoolStats getPoolStats() {
        return null;
    }

    /**
     * Get multi thread pool stats.
     *
     * @return thead pools stats
     */
    default List<ThreadPoolStats> getMultiPoolStats() {
        return Collections.emptyList();
    }

    /**
     * Check update params.
     * @param properties
     */
    default void checkParams(SimpleTpProperties properties) {
        if (properties.getCorePoolSize() < 0 ||
                properties.getMaximumPoolSize() <= 0 ||
                properties.getMaximumPoolSize() < properties.getCorePoolSize() ||
                properties.getKeepAliveTime() < 0) {
            throw new IllegalArgumentException();
        }
    }
}
