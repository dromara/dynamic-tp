package com.dtp.adapter.webserver.listener;

import com.dtp.adapter.TpHandler;
import com.dtp.adapter.webserver.handler.AbstractWebServerTpHandler;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.event.CollectEvent;
import com.dtp.core.handler.CollectorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * WebServerCollectListener related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public class WebServerCollectListener implements ApplicationListener<CollectEvent> {

    @Override
    public void onApplicationEvent(@NonNull CollectEvent event) {
        DtpProperties dtpProperties = event.getDtpProperties();
        try {
            TpHandler webServerTpHandler = ApplicationContextHolder.getBean(AbstractWebServerTpHandler.class);
            Optional.ofNullable(webServerTpHandler.getPoolStats())
                    .ifPresent(p -> CollectorHandler.getInstance().collect(p, dtpProperties.getCollectorType()));
        } catch (Exception e) {
            log.error("DynamicTp monitor, collect web server thread pool metrics failed.", e);
        }
    }
}