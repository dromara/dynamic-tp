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
     * @return
     */
    Executor getWebServerTp();

    /**
     * Get thread pool stats.
     * @return
     */
    ThreadPoolStats getPoolStats();

    /**
     * Update web server thread pool.
     * @param dtpProperties dtpProperties
     */
    void updateWebServerTp(DtpProperties dtpProperties);
}
