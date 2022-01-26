package com.dtp.adapter.web.handler;

import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.web.UndertowThreadPool;
import com.dtp.common.dto.ThreadPoolStats;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServer;
import org.springframework.boot.web.server.WebServer;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * UndertowTpHandler related
 *
 * @author yanhom
 * @since 1.0.0
 */
public class UndertowTpHandler extends AbstractWebServerTpHandler {

    @Override
    public Executor doGetTp(WebServer webServer) {
        UndertowServletWebServer undertowWebServer = (UndertowServletWebServer) webServer;
        // TODO: if get null, then the XNIO Worker TaskPool will be used.
        return undertowWebServer.getDeploymentManager().getDeployment().getExecutor();
    }

    @Override
    public ThreadPoolStats getPoolStats() {
        // TODO: if doGetTp return null, then XNIO Worker TaskPool will be used.
        return null;
    }

    @Override
    public void updateWebServerTp(DtpProperties dtpProperties) {
        UndertowThreadPool undertowTp = dtpProperties.getUndertowTp();
        if (Objects.isNull(undertowTp) || Objects.isNull(webServerExecutor)) {
            return;
        }
        // TODO update undertow thread pool
    }
}
