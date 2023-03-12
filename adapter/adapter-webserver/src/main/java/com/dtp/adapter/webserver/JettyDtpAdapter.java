package com.dtp.adapter.webserver;

import com.dtp.common.properties.DtpProperties;
import com.dtp.common.entity.TpExecutorProps;
import com.dtp.common.entity.TpMainFields;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.common.entity.ThreadPoolStats;
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
        TpExecutorProps props = dtpProperties.getJettyTp();
        if (Objects.isNull(props) || containsInvalidParams(props, log)) {
            return;
        }
        Executor executor = getExecutor();
        if (Objects.isNull(executor)) {
            return;
        }

        ThreadPool.SizedThreadPool threadPool = (ThreadPool.SizedThreadPool) executor;
        int oldCoreSize = threadPool.getMinThreads();
        int oldMaxSize = threadPool.getMaxThreads();
        TpMainFields oldFields = ExecutorConverter.ofSimple(POOL_NAME, oldCoreSize, oldMaxSize, 0L);
        doRefresh(threadPool, props);
        TpMainFields newFields = ExecutorConverter.ofSimple(props.getThreadPoolName(), threadPool.getMinThreads(),
                threadPool.getMaxThreads(), 0L);
        if (oldFields.equals(newFields)) {
            log.warn("DynamicTp adapter refresh, main properties of [{}] have not changed.", POOL_NAME);
            return;
        }
        log.info("DynamicTp adapter [{}] refreshed end, corePoolSize: [{}], maxPoolSize: [{}]",
                POOL_NAME,
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldCoreSize, newFields.getCorePoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldMaxSize, newFields.getMaxPoolSize()));
    }

    private void doRefresh(ThreadPool.SizedThreadPool threadPool, TpExecutorProps props) {
        if (props.getMaximumPoolSize() < threadPool.getMaxThreads()) {
            if (!Objects.equals(threadPool.getMinThreads(), props.getCorePoolSize())) {
                threadPool.setMinThreads(props.getCorePoolSize());
            }
            if (!Objects.equals(threadPool.getMaxThreads(), props.getMaximumPoolSize())) {
                threadPool.setMaxThreads(props.getMaximumPoolSize());
            }
            return;
        }

        if (!Objects.equals(threadPool.getMaxThreads(), props.getMaximumPoolSize())) {
            threadPool.setMaxThreads(props.getMaximumPoolSize());
        }
        if (!Objects.equals(threadPool.getMinThreads(), props.getCorePoolSize())) {
            threadPool.setMinThreads(props.getCorePoolSize());
        }
    }
}
