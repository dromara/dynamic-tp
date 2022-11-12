package com.dtp.adapter.webserver;

import com.dtp.common.properties.DtpProperties;
import com.dtp.common.properties.SimpleTpProperties;
import com.dtp.common.dto.DtpMainProp;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.dto.ThreadPoolStats;
import com.dtp.core.convert.ExecutorConverter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.springframework.boot.web.embedded.jetty.JettyWebServer;
import org.springframework.boot.web.server.WebServer;

import java.util.Objects;
import java.util.concurrent.Executor;

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

        Executor executor = getExecutor();
        if (Objects.isNull(executor)) {
            return null;
        }

        ThreadPool.SizedThreadPool threadPool = (ThreadPool.SizedThreadPool) executor;
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
        if (Objects.isNull(properties) || containsInvalidParams(properties, log)) {
            return;
        }
        Executor executor = getExecutor();
        if (Objects.isNull(executor)) {
            return;
        }

        ThreadPool.SizedThreadPool threadPool = (ThreadPool.SizedThreadPool) executor;
        int oldCoreSize = threadPool.getMinThreads();
        int oldMaxSize = threadPool.getMaxThreads();
        DtpMainProp oldProp = ExecutorConverter.ofSimple(POOL_NAME, oldCoreSize, oldMaxSize, 0L);
        doRefresh(threadPool, properties);
        DtpMainProp newProp = ExecutorConverter.ofSimple(properties.getThreadPoolName(), threadPool.getMinThreads(),
                threadPool.getMaxThreads(), 0L);
        if (oldProp.equals(newProp)) {
            log.warn("DynamicTp adapter refresh, main properties of [{}] have not changed.", POOL_NAME);
            return;
        }
        log.info("DynamicTp adapter [{}] refreshed end, corePoolSize: [{}], maxPoolSize: [{}]",
                POOL_NAME,
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldCoreSize, newProp.getCorePoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldMaxSize, newProp.getMaxPoolSize()));
    }

    private void doRefresh(ThreadPool.SizedThreadPool threadPool, SimpleTpProperties properties) {
        if (properties.getMaximumPoolSize() < threadPool.getMaxThreads()) {
            if (!Objects.equals(threadPool.getMinThreads(), properties.getCorePoolSize())) {
                threadPool.setMinThreads(properties.getCorePoolSize());
            }
            if (!Objects.equals(threadPool.getMaxThreads(), properties.getMaximumPoolSize())) {
                threadPool.setMaxThreads(properties.getMaximumPoolSize());
            }
            return;
        }

        if (!Objects.equals(threadPool.getMaxThreads(), properties.getMaximumPoolSize())) {
            threadPool.setMaxThreads(properties.getMaximumPoolSize());
        }
        if (!Objects.equals(threadPool.getMinThreads(), properties.getCorePoolSize())) {
            threadPool.setMinThreads(properties.getCorePoolSize());
        }
    }
}