package com.dtp.core.support;

import com.dtp.common.entity.ThreadPoolStats;

import java.util.Collections;
import java.util.List;

/**
 * MetricsAware related
 *
 * @author yanhom
 * @since 1.0.9
 */
public interface MetricsAware {

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
