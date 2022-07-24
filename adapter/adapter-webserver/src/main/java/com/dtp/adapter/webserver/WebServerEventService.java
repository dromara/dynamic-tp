package com.dtp.adapter.webserver;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.core.adapter.DtpAdapter;
import com.dtp.core.adapter.DtpAdapterListener;
import com.dtp.core.handler.CollectorHandler;

import java.util.Optional;

/**
 * WebServerEventService related
 *
 * @author yanhom
 * @since 1.0.6
 */
public class WebServerEventService extends DtpAdapterListener {

    @Override
    protected void doCollect(DtpProperties dtpProperties) {
        DtpAdapter dtpAdapter = ApplicationContextHolder.getBean(AbstractWebServerDtpAdapter.class);
        Optional.ofNullable(dtpAdapter.getPoolStats())
                .ifPresent(p -> CollectorHandler.getInstance().collect(p, dtpProperties.getCollectorType()));
    }
}
