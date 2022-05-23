package com.dtp.adapter.dubbo.webserver.handler;

import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.SimpleTpProperties;
import com.dtp.common.dto.ThreadPoolStats;
import com.dtp.common.ex.DtpException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.WebServer;

import java.util.Objects;
import java.util.concurrent.Executor;

import static com.dtp.common.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;

/**
 * TomcatTpHandler related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public class TomcatDtpHandler extends AbstractWebServerDtpHandler {

    private static final String POOL_NAME = "tomcatWebServerTp";

    @Override
    public Executor doGetTp(WebServer webServer) {
        TomcatWebServer tomcatWebServer = (TomcatWebServer) webServer;
        return tomcatWebServer.getTomcat().getConnector().getProtocolHandler().getExecutor();
    }

    @Override
    public ThreadPoolStats getPoolStats() {
        ThreadPoolExecutor executor = convertAndGet();
        return ThreadPoolStats.builder()
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(executor.getMaximumPoolSize())
                .queueType(executor.getQueue().getClass().getSimpleName())
                .queueCapacity(executor.getQueue().size() + executor.getQueue().remainingCapacity())
                .queueSize(executor.getQueue().size())
                .queueRemainingCapacity(executor.getQueue().remainingCapacity())
                .activeCount(executor.getActiveCount())
                .taskCount(executor.getTaskCount())
                .completedTaskCount(executor.getCompletedTaskCount())
                .largestPoolSize(executor.getLargestPoolSize())
                .poolSize(executor.getPoolSize())
                .waitTaskCount(executor.getQueue().size())
                .poolName(POOL_NAME)
                .build();
    }

    @Override
    public void updateTp(DtpProperties dtpProperties) {
        SimpleTpProperties tomcatTp = dtpProperties.getTomcatTp();
        if (Objects.isNull(tomcatTp)) {
            return;
        }

        checkParams(tomcatTp);
        int oldCoreSize = convertAndGet().getCorePoolSize();
        int oldMaxSize = convertAndGet().getMaximumPoolSize();

        convertAndGet().setCorePoolSize(tomcatTp.getCorePoolSize());
        convertAndGet().setMaximumPoolSize(tomcatTp.getMaximumPoolSize());

        log.info("DynamicTp tomcatWebServerTp refreshed end, coreSize: [{}], maxSize: [{}]",
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldCoreSize, tomcatTp.getCorePoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldMaxSize, tomcatTp.getMaximumPoolSize()));
    }

    private ThreadPoolExecutor convertAndGet() {
        Executor executor = getExecutor();
        if (Objects.isNull(executor)) {
            log.warn("Tomcat web server threadPool is null.");
            throw new DtpException("Tomcat web server threadPool is null.");
        }
        return (ThreadPoolExecutor) executor;
    }
}
