package com.dtp.adapter.web.listener;

import com.dtp.adapter.web.handler.WebServerTpHandler;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.event.RefreshEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

/**
 * DtpWebRefreshListener related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class DtpWebRefreshListener implements ApplicationListener<RefreshEvent> {

    @Override
    public void onApplicationEvent(RefreshEvent event) {

        ApplicationContext applicationContext = ApplicationContextHolder.getInstance();
        if (!(applicationContext instanceof WebServerApplicationContext)) {
            return;
        }

        WebServerTpHandler webServerTpHandler = ApplicationContextHolder.getBean(WebServerTpHandler.class);
        webServerTpHandler.updateWebServerTp(event.getDtpProperties());
    }
}