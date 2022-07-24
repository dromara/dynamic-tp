package com.dtp.adapter.webserver;

import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.SimpleTpProperties;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.dto.ThreadPoolStats;
import com.dtp.common.ex.DtpException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.springframework.boot.web.embedded.jetty.JettyWebServer;
import org.springframework.boot.web.server.WebServer;

import java.util.Objects;

import static com.dtp.common.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;

/**
 * JettyDtpAdapter related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public class JettyDtpAdapter extends AbstractWebServerDtpAdapter {

    private static final String POOL_NAME = "jettyTp";

    @Override
    public ExecutorWrapper doGetExecutorWrapper(WebServer webServer) {
        JettyWebServer jettyWebServer = (JettyWebServer) webServer;
        return new ExecutorWrapper(POOL_NAME, jettyWebServer.getServer().getThreadPool());
    }

    @Override
    public ThreadPoolStats getPoolStats() {
        ThreadPool.SizedThreadPool threadPool = (ThreadPool.SizedThreadPool) getWrapper().getExecutor();
        ThreadPoolStats poolStats = ThreadPoolStats.builder()
                .corePoolSize(threadPool.getMinThreads())
                .maximumPoolSize(threadPool.getMaxThreads())
                .poolName(POOL_NAME)
                .build();

        if (threadPool instanceof QueuedThreadPool) {
            QueuedThreadPool queuedThreadPool = (QueuedThreadPool) threadPool;
            poolStats.setActiveCount(queuedThreadPool.getBusyThreads());
            poolStats.setQueueSize(queuedThreadPool.getQueueSize());
            poolStats.setPoolSize(queuedThreadPool.getThreads());
        }
        return poolStats;
    }

    @Override
    public void refresh(DtpProperties dtpProperties) {
        SimpleTpProperties properties = dtpProperties.getJettyTp();
        if (Objects.isNull(properties)) {
            return;
        }

        val executorWrapper = getWrapper();
        ThreadPool.SizedThreadPool threadPool = (ThreadPool.SizedThreadPool) executorWrapper.getExecutor();
        int oldCoreSize = threadPool.getMinThreads();
        int oldMaxSize = threadPool.getMaxThreads();
        checkParams(oldMaxSize, properties);

        threadPool.setMinThreads(properties.getCorePoolSize());
        threadPool.setMaxThreads(properties.getMaximumPoolSize());

        log.info("DynamicTp adapter [{}}] refreshed end, corePoolSize: [{}], maxPoolSize: [{}]",
                POOL_NAME,
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldCoreSize, properties.getCorePoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldMaxSize, properties.getMaximumPoolSize()));
    }

    private ExecutorWrapper getWrapper() {
        ExecutorWrapper executorWrapper = getExecutorWrapper();
        if (Objects.isNull(executorWrapper) || Objects.isNull(executorWrapper.getExecutor())) {
            log.warn("Jetty web server threadPool is null.");
            throw new DtpException("Jetty web server threadPool is null.");
        }
        return executorWrapper;
    }
}