package com.dtp.adapter.web.handler;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.Executor;

/**
 * AbstractWebServerTpHandler related
 *
 * @author yanhom
 * @since 1.0.0
 */
public abstract class AbstractWebServerTpHandler implements WebServerTpHandler, InitializingBean {

    protected volatile Executor webServerExecutor;

    @Override
    public Executor getWebServerTp() {
        if (webServerExecutor == null) {
            synchronized (AbstractWebServerTpHandler.class) {
                if (webServerExecutor == null) {
                    ApplicationContext applicationContext = ApplicationContextHolder.getInstance();
                    WebServer webServer = ((WebServerApplicationContext) applicationContext).getWebServer();
                    webServerExecutor = doGetTp(webServer);
                }
            }
        }
        return webServerExecutor;
    }

    /**
     * Get thread pool.
     *
     * @param webServer webServer
     * @return the Executor instance
     */
    protected abstract Executor doGetTp(WebServer webServer);

    @Override
    public void afterPropertiesSet() {
        DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
        updateWebServerTp(dtpProperties);
    }
}
