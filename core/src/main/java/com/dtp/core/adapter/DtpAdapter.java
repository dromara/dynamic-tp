package com.dtp.core.adapter;

import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.SimpleTpProperties;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.dto.ThreadPoolStats;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * DtpAdapter related
 *
 * @author yanhom
 * @since 1.0.6
 */
public interface DtpAdapter {

    /**
     * Get specify thread pool executor wrapper.
     *
     * @return specify executor
     */
    default ExecutorWrapper getExecutorWrapper() {
        return null;
    }

    /**
     * Get executor wrappers.
     *
     * @return executors
     */
    default Map<String, ExecutorWrapper> getExecutorWrappers() {
        return Collections.emptyMap();
    }

    /**
     * Refresh the thread pool with specify properties.
     *
     * @param dtpProperties the targeted dtpProperties
     */
    void refresh(DtpProperties dtpProperties);

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
     *
     * @param oldMaxPoolSize old maxPoolSize
     * @param properties the targeted properties
     */
    default void checkParams(int oldMaxPoolSize, SimpleTpProperties properties) {
        if (properties.getCorePoolSize() < 0 ||
                properties.getMaximumPoolSize() <= 0 ||
                properties.getMaximumPoolSize() < properties.getCorePoolSize() ||
                properties.getKeepAliveTime() < 0) {
            throw new IllegalArgumentException("Invalid thread pool params.");
        }

        if (oldMaxPoolSize < properties.getCorePoolSize()) {
            throw new IllegalArgumentException("New corePoolSize cannot greater than current maximumPoolSize.");
        }
    }
}
