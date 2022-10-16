package com.dtp.adapter.webserver;

import com.dtp.adapter.common.DtpAdapter;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.dto.ThreadPoolStats;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import java.util.List;

/**
 * AbstractWebServerDtpAdapter related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractWebServerDtpAdapter implements
        DtpAdapter, ApplicationListener<WebServerInitializedEvent> {

    protected ExecutorWrapper executorWrapper;

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        try {
            DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
            initialize();
            refresh(dtpProperties);
        } catch (Exception e) {
            log.error("Init web server thread pool failed.", e);
        }
    }

    @Override
    public ExecutorWrapper getExecutorWrapper() {
        return executorWrapper;
    }

    @Override
    public List<ThreadPoolStats> getMultiPoolStats() {
        return Lists.newArrayList(getPoolStats());
    }

    protected void initialize() {
        if (executorWrapper == null) {
            ApplicationContext applicationContext = ApplicationContextHolder.getInstance();
            WebServer webServer = ((WebServerApplicationContext) applicationContext).getWebServer();
            executorWrapper = doGetExecutorWrapper(webServer);
            log.info("DynamicTp adapter, web server executor init end, executor: {}", executorWrapper.getExecutor());
        }
    }

    /**
     * Get thread pool executor wrapper.
     *
     * @param webServer webServer
     * @return the Executor instance
     */
    protected abstract ExecutorWrapper doGetExecutorWrapper(WebServer webServer);
}
