package com.dtp.adapter.web.handler;

import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.web.TomcatThreadPool;
import com.dtp.common.dto.ThreadPoolStats;
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
public class TomcatTpHandler extends AbstractWebServerTpHandler {

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
                .rejectHandlerName(executor.getRejectedExecutionHandler().getClass().getSimpleName())
                .dtpName("tomcatWebServerTp")
                .build();
    }

    @Override
    public void updateWebServerTp(DtpProperties dtpProperties) {
        TomcatThreadPool tomcatTp = dtpProperties.getTomcatTp();
        if (Objects.isNull(tomcatTp)) {
            return;
        }

        int oldMinSpare = convertAndGet().getCorePoolSize();
        int oldMax = convertAndGet().getMaximumPoolSize();

        convertAndGet().setCorePoolSize(tomcatTp.getMinSpare());
        convertAndGet().setMaximumPoolSize(tomcatTp.getMax());

        log.info("DynamicTp tomcatWebServerTp refreshed end, minSpare: [{}], max: [{}]",
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldMinSpare, tomcatTp.getMinSpare()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldMax, tomcatTp.getMax()));
    }

    private ThreadPoolExecutor convertAndGet() {
        return (ThreadPoolExecutor) getWebServerTp();
    }
}
