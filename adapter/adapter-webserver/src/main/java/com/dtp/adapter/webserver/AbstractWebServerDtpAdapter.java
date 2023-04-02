package com.dtp.adapter.webserver;

import com.dtp.adapter.common.AbstractDtpAdapter;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.entity.ThreadPoolStats;
import com.dtp.common.properties.DtpProperties;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.support.ExecutorAdapter;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * AbstractWebServerDtpAdapter related
 *
 * @author yanhom
 * @author dragon-zhang
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractWebServerDtpAdapter<A extends Executor>
        extends AbstractDtpAdapter {

    protected ExecutorWrapper executorWrapper;

    @Override
    public boolean supportsEventType(ResolvableType resolvableType) {
        Class<?> type = resolvableType.getRawClass();
        if (type != null) {
            return WebServerInitializedEvent.class.isAssignableFrom(type);
        }
        return false;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof WebServerInitializedEvent) {
            try {
                DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
                initialize();
                refresh(dtpProperties);
            } catch (Exception e) {
                log.error("Init web server thread pool failed.", e);
            }
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

    @Override
    protected void initialize() {
        if (executorWrapper == null) {
            ApplicationContext applicationContext = ApplicationContextHolder.getInstance();
            WebServer webServer = ((WebServerApplicationContext) applicationContext).getWebServer();
            executorWrapper = doGetExecutorWrapper(webServer);
            initNotifyItems(executorWrapper.getThreadPoolName(), executorWrapper);
            log.info("DynamicTp adapter, web server executor init end, executor: {}", executorWrapper.getExecutor());
        }
    }

    @SuppressWarnings("unchecked")
    protected ExecutorAdapter<A> getExecutor() {
        ExecutorWrapper wrapper = getExecutorWrapper();
        if (Objects.isNull(wrapper) || Objects.isNull(wrapper.getExecutor())) {
            log.warn("Web server threadPool is null.");
            return null;
        }
        return (ExecutorAdapter<A>) wrapper.getExecutor();
    }

    /**
     * Get thread pool executor wrapper.
     *
     * @param webServer webServer
     * @return the Executor instance
     */
    protected abstract ExecutorWrapper doGetExecutorWrapper(WebServer webServer);
}
