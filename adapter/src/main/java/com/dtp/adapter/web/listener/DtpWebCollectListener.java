package com.dtp.adapter.web.listener;

import com.dtp.common.event.CollectEvent;
import com.dtp.adapter.web.handler.WebServerTpHandler;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.dto.ThreadPoolStats;
import com.dtp.core.handler.CollectorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;

/**
 * DtpWebCollectListener related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public class DtpWebCollectListener implements ApplicationListener<CollectEvent> {

    @Override
    public void onApplicationEvent(CollectEvent event) {
        DtpProperties dtpProperties = event.getDtpProperties();
        if (!dtpProperties.isEnabledCollect()) {
            return;
        }
        try {
            WebServerTpHandler webServerTpHandler = ApplicationContextHolder.getBean(WebServerTpHandler.class);
            ThreadPoolStats poolStats = webServerTpHandler.getPoolStats();
            if (poolStats == null) {
                return;
            }
            CollectorHandler.getInstance().collect(poolStats, dtpProperties.getCollectorType());
        } catch (Exception e) {
            log.error("DynamicTp monitor, collect web server thread pool metrics error...", e);
        }
    }
}