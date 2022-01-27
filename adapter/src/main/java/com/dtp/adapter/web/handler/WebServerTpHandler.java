package com.dtp.adapter.web.handler;

import com.dtp.common.config.DtpProperties;
import com.dtp.common.dto.ThreadPoolStats;

import java.util.concurrent.Executor;

/**
 * WebServerTpHandler related
 *
 * @author yanhom
 * @since 1.0.0
 */
public interface WebServerTpHandler {

    /**
     * Get specify web server thread pool.
     *
     * @return the specify executor
     */
    Executor getWebServerTp();

    /**
     * Get web server thread pool stats.
     *
     * @return the thread pool stats
     */
    ThreadPoolStats getPoolStats();

    /**
     * Update web server thread pool.
     *
     * @param dtpProperties the targeted dtpProperties
     */
    void updateWebServerTp(DtpProperties dtpProperties);
}
