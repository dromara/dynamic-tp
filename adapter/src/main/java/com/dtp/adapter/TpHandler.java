package com.dtp.adapter;

import com.dtp.common.config.DtpProperties;
import com.dtp.common.dto.ThreadPoolStats;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * TpHandler related
 *
 * @author yanhom
 * @since 1.0.6
 */
public interface TpHandler {

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
    default List<Executor> getExecutors() {
        return Collections.emptyList();
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
}
