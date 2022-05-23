package com.dtp.adapter.dubbo.webserver.handler;

import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.SimpleTpProperties;
import com.dtp.common.dto.ThreadPoolStats;
import com.dtp.common.ex.DtpException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.springframework.boot.web.embedded.jetty.JettyWebServer;
import org.springframework.boot.web.server.WebServer;

import java.util.Objects;
import java.util.concurrent.Executor;

import static com.dtp.common.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;

/**
 * JettyTpHandler related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public class JettyDtpHandler extends AbstractWebServerDtpHandler {

    private static final String POOL_NAME = "jettyWebServerTp";

    @Override
    public Executor doGetTp(WebServer webServer) {
        JettyWebServer jettyWebServer = (JettyWebServer) webServer;
        return jettyWebServer.getServer().getThreadPool();
    }

    @Override
    public ThreadPoolStats getPoolStats() {
        ThreadPool.SizedThreadPool threadPool = convertAndGet();
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
    public void updateTp(DtpProperties dtpProperties) {
        SimpleTpProperties jettyTp = dtpProperties.getJettyTp();
        if (Objects.isNull(jettyTp)) {
            return;
        }

        checkParams(jettyTp);
        int oldMinThreads = convertAndGet().getMinThreads();
        int oldMaxThreads = convertAndGet().getMaxThreads();

        convertAndGet().setMinThreads(jettyTp.getCorePoolSize());
        convertAndGet().setMaxThreads(jettyTp.getMaximumPoolSize());

        log.info("DynamicTp jettyWebServerTp refreshed end, coreSize: [{}], maxSize: [{}]",
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldMinThreads, jettyTp.getCorePoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldMaxThreads, jettyTp.getMaximumPoolSize()));
    }

    private ThreadPool.SizedThreadPool convertAndGet() {
        Executor executor = getExecutor();
        if (Objects.isNull(executor)) {
            log.warn("Jetty web server threadPool is null.");
            throw new DtpException("Jetty web server threadPool is null.");
        }
        return (ThreadPool.SizedThreadPool) executor;
    }
}