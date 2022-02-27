package com.dtp.adapter.web.handler;

import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.web.JettyThreadPool;
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
public class JettyTpHandler extends AbstractWebServerTpHandler {

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
                .dtpName("jettyWebServerTp")
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
    public void updateWebServerTp(DtpProperties dtpProperties) {
        JettyThreadPool jettyTp = dtpProperties.getJettyTp();
        if (Objects.isNull(jettyTp)) {
            return;
        }

        int oldMinThreads = convertAndGet().getMinThreads();
        int oldMaxThreads = convertAndGet().getMaxThreads();

        convertAndGet().setMinThreads(jettyTp.getMin());
        convertAndGet().setMaxThreads(jettyTp.getMax());

        log.info("DynamicTp jettyWebServerTp refreshed end, minThreads: [{}], maxThreads: [{}]",
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldMinThreads, jettyTp.getMin()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldMaxThreads, jettyTp.getMax()));
    }

    private ThreadPool.SizedThreadPool convertAndGet() {
        Executor executor = getWebServerTp();
        if (Objects.isNull(executor)) {
            log.warn("Jetty web server threadPool is null.");
            throw new DtpException("Jetty web server threadPool is null.");
        }
        return (ThreadPool.SizedThreadPool) executor;
    }
}