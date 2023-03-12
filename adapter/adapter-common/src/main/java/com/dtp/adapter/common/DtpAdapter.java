package com.dtp.adapter.common;

import com.dtp.common.properties.DtpProperties;
import com.dtp.common.entity.TpExecutorProps;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.support.MetricsAware;
import org.slf4j.Logger;

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
     * @param props the targeted properties
     * @param log logger
     * @return true or false
     */
    default boolean containsInvalidParams(TpExecutorProps props, Logger log) {
        if (props.getCorePoolSize() < 0
                || props.getMaximumPoolSize() <= 0
                || props.getMaximumPoolSize() < props.getCorePoolSize()
                || props.getKeepAliveTime() < 0) {
            log.error("DynamicTp adapter refresh, invalid parameters exist, properties: {}", props);
            return true;
        }
        return false;
    }
}
