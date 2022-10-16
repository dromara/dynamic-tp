package com.dtp.adapter.common;

import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.SimpleTpProperties;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.core.support.MetricsAware;

import java.util.Collections;
import java.util.Map;

/**
 * DtpAdapter related
 *
 * @author yanhom
 * @since 1.0.6
 */
public interface DtpAdapter extends MetricsAware {

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
     * Check update params.
     *
     * @param oldMaxPoolSize old maxPoolSize
     * @param properties the targeted properties
     */
    default void checkRefreshParams(int oldMaxPoolSize, SimpleTpProperties properties) {
        if (properties.getCorePoolSize() < 0 ||
                properties.getMaximumPoolSize() <= 0 ||
                properties.getMaximumPoolSize() < properties.getCorePoolSize() ||
                properties.getKeepAliveTime() < 0) {
            throw new IllegalArgumentException("Invalid thread pool params.");
        }

        if (oldMaxPoolSize < properties.getCorePoolSize()) {
            throw new IllegalArgumentException(String.format("New corePoolSize [%d] cannot greater than " +
                    "current maximumPoolSize [%d]", properties.getCorePoolSize(), oldMaxPoolSize));
        }
    }
}
