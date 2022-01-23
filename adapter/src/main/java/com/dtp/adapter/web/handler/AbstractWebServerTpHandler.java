package com.dtp.adapter.web.handler;

import com.dtp.common.ApplicationContextHolder;
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
public abstract class AbstractWebServerTpHandler implements WebServerTpHandler {

    protected Executor webServerExecutor;

    protected AbstractWebServerTpHandler() {
        init();
    }

    private void init() {
        ApplicationContext applicationContext = ApplicationContextHolder.getInstance();
        if (webServerExecutor == null) {
            WebServer webServer = ((WebServerApplicationContext) applicationContext).getWebServer();
            webServerExecutor = doGetTp(webServer);
        }
    }

    @Override
    public Executor getWebServerTp() {
        return webServerExecutor;
    }

    /**
     * Get thread pool.
     * @param webServer webServer
     * @return Executor instance
     */
    protected abstract Executor doGetTp(WebServer webServer);

}
