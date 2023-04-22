package com.dtp.adapter.webserver;

import com.dtp.adapter.common.AbstractDtpAdapter;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.properties.DtpProperties;
import com.dtp.core.support.ExecutorWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;

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
    protected void initialize() {
        if (executors.get(getTpName()) == null) {
            ApplicationContext applicationContext = ApplicationContextHolder.getInstance();
            WebServer webServer = ((WebServerApplicationContext) applicationContext).getWebServer();
            ExecutorWrapper wrapper = doInitExecutorWrapper(webServer);
            initNotifyItems(wrapper.getThreadPoolName(), wrapper);
            executors.put(getTpName(), wrapper);
            log.info("DynamicTp adapter, web server executor init end, executor: {}", wrapper.getExecutor());
        }
    }

    /**
     * Do init thread pool executor wrapper.
     *
     * @param webServer webServer
     * @return the Executor instance
     */
    protected abstract ExecutorWrapper doInitExecutorWrapper(WebServer webServer);

    /**
     * Refresh thread pool executor wrapper.
     * @return the thread pool name
     */
    protected abstract String getTpName();
}
