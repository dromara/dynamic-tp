package com.dtp.adapter.webserver.handler;

import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.SimpleTpProperties;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.dto.ThreadPoolStats;
import com.dtp.common.ex.DtpException;
import com.dtp.common.util.ReflectionUtil;
import io.undertow.Undertow;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.web.embedded.undertow.UndertowWebServer;
import org.springframework.boot.web.server.WebServer;
import org.xnio.Options;
import org.xnio.XnioWorker;
import org.xnio.management.XnioWorkerMXBean;

import java.io.IOException;
import java.util.Objects;

import static com.dtp.common.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;

/**
 * UndertowTpHandler related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public class UndertowDtpHandler extends AbstractWebServerDtpHandler {

    private static final String POOL_NAME = "undertowTp";

    @Override
    public ExecutorWrapper doGetExecutorWrapper(WebServer webServer) {

        UndertowWebServer undertowWebServer = (UndertowWebServer) webServer;
        val undertow = (Undertow) ReflectionUtil.getField(UndertowWebServer.class, "undertow", undertowWebServer);
        if (Objects.isNull(undertow)) {
            return null;
        }
        return new ExecutorWrapper(POOL_NAME, undertow.getWorker());
    }

    @Override
    public ThreadPoolStats getPoolStats() {
        XnioWorker xnioWorker = (XnioWorker) getWrapper().getExecutor();
        XnioWorkerMXBean mxBean = xnioWorker.getMXBean();
        return ThreadPoolStats.builder()
                .corePoolSize(mxBean.getCoreWorkerPoolSize())
                .maximumPoolSize(mxBean.getMaxWorkerPoolSize())
                .poolSize(mxBean.getWorkerPoolSize())
                .activeCount(mxBean.getBusyWorkerThreadCount())
                .queueSize(mxBean.getWorkerQueueSize())
                .poolName(POOL_NAME)
                .build();
    }

    @Override
    public void refresh(DtpProperties dtpProperties) {
        SimpleTpProperties properties = dtpProperties.getUndertowTp();
        if (Objects.isNull(properties)) {
            return;
        }

        checkParams(properties);
        val executorWrapper = getWrapper();
        XnioWorker xnioWorker = (XnioWorker) executorWrapper.getExecutor();

        try {
            int oldCorePoolSize = xnioWorker.getOption(Options.WORKER_TASK_CORE_THREADS);
            int oldMaxPoolSize = xnioWorker.getOption(Options.WORKER_TASK_MAX_THREADS);
            int oldKeepAliveTime = xnioWorker.getOption(Options.WORKER_TASK_KEEPALIVE);

            int keepAlive = properties.getKeepAliveTime() * 1000;
            xnioWorker.setOption(Options.WORKER_TASK_CORE_THREADS, properties.getCorePoolSize());
            xnioWorker.setOption(Options.WORKER_TASK_MAX_THREADS, properties.getMaximumPoolSize());
            xnioWorker.setOption(Options.WORKER_TASK_KEEPALIVE, keepAlive);

            log.info("DynamicTp adapter [{}}] refreshed end, corePoolSize: [{}], maxPoolSize: [{}], " +
                            "keepAliveTime: [{}]", POOL_NAME,
                    String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldCorePoolSize, properties.getCorePoolSize()),
                    String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldMaxPoolSize, properties.getMaximumPoolSize()),
                    String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldKeepAliveTime, keepAlive));
        } catch (IOException e) {
            log.error("Update undertow web server threadPool failed.", e);
        }
    }

    private ExecutorWrapper getWrapper() {
        ExecutorWrapper executorWrapper = getExecutorWrapper();
        if (Objects.isNull(executorWrapper) || Objects.isNull(executorWrapper.getExecutor())) {
            log.warn("Undertow web server threadPool is null.");
            throw new DtpException("Undertow web server threadPool is null.");
        }
        return executorWrapper;
    }
}
