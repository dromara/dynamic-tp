package com.dtp.adapter.webserver;

import com.dtp.adapter.common.AbstractDtpHandleListener;
import com.dtp.adapter.common.DtpHandler;
import com.dtp.adapter.webserver.handler.AbstractWebServerDtpHandler;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.core.handler.CollectorHandler;

import java.util.Optional;

/**
 * WebServerEventService related
 *
 * @author yanhom
 * @since 1.0.6
 */
public class WebServerEventService extends AbstractDtpHandleListener {

    @Override
    protected void doCollect(DtpProperties dtpProperties) {
        DtpHandler webServerTpHandler = ApplicationContextHolder.getBean(AbstractWebServerDtpHandler.class);
        Optional.ofNullable(webServerTpHandler.getPoolStats())
                .ifPresent(p -> CollectorHandler.getInstance().collect(p, dtpProperties.getCollectorType()));
    }

    @Override
    protected void doRefresh(DtpProperties dtpProperties) {
        DtpHandler webServerTpHandler = ApplicationContextHolder.getBean(AbstractWebServerDtpHandler.class);
        webServerTpHandler.refresh(dtpProperties);
    }
}
