package com.dtp.adapter.web.listener;

import com.dtp.adapter.web.handler.WebServerTpHandler;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.event.RefreshEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

/**
 * DtpWebRefreshListener related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpWebRefreshListener implements ApplicationListener<RefreshEvent> {

    @Override
    public void onApplicationEvent(RefreshEvent event) {

        ApplicationContext applicationContext = ApplicationContextHolder.getInstance();
        if (!(applicationContext instanceof WebServerApplicationContext)) {
            return;
        }

        try {
            WebServerTpHandler webServerTpHandler = ApplicationContextHolder.getBean(WebServerTpHandler.class);
            webServerTpHandler.updateWebServerTp(event.getDtpProperties());
        } catch (Exception e) {
            log.error("DynamicTp refresh, update web server thread pool failed.", e);
        }
    }
}