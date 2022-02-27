package com.dtp.adapter.web.handler;

import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.web.UndertowThreadPool;
import com.dtp.common.dto.ThreadPoolStats;
import com.dtp.common.ex.DtpException;
import io.undertow.Undertow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.undertow.UndertowWebServer;
import org.springframework.boot.web.server.WebServer;
import org.springframework.util.ReflectionUtils;
import org.xnio.Options;
import org.xnio.XnioWorker;
import org.xnio.management.XnioWorkerMXBean;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.Executor;

import static com.dtp.common.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;

/**
 * UndertowTpHandler related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public class UndertowTpHandler extends AbstractWebServerTpHandler {

    @Override
    public Executor doGetTp(WebServer webServer) {

        UndertowWebServer undertowWebServer = (UndertowWebServer) webServer;
        Field undertowField = ReflectionUtils.findField(UndertowWebServer.class, "undertow");
        ReflectionUtils.makeAccessible(undertowField);
        Undertow undertow = (Undertow) ReflectionUtils.getField(undertowField, undertowWebServer);
        if (Objects.isNull(undertow)) {
            return null;
        }
        return undertow.getWorker();
    }

    @Override
    public ThreadPoolStats getPoolStats() {
        XnioWorker xnioWorker = convertAndGet();
        XnioWorkerMXBean mxBean = xnioWorker.getMXBean();
        return ThreadPoolStats.builder()
                .corePoolSize(mxBean.getCoreWorkerPoolSize())
                .maximumPoolSize(mxBean.getMaxWorkerPoolSize())
                .poolSize(mxBean.getWorkerPoolSize())
                .activeCount(mxBean.getBusyWorkerThreadCount())
                .queueSize(mxBean.getWorkerQueueSize())
                .dtpName("undertowWebServerWorkerTp")
                .build();
    }

    @Override
    public void updateWebServerTp(DtpProperties dtpProperties) {
        UndertowThreadPool undertowTp = dtpProperties.getUndertowTp();
        if (Objects.isNull(undertowTp)) {
            return;
        }

        XnioWorker xnioWorker = convertAndGet();
        try {
            int oldIoThreads = xnioWorker.getOption(Options.WORKER_IO_THREADS);
            int oldCoreWorkerThreads = xnioWorker.getOption(Options.WORKER_TASK_CORE_THREADS);
            int oldMaxWorkerThreads = xnioWorker.getOption(Options.WORKER_TASK_MAX_THREADS);
            int oldWorkerKeepAlive = xnioWorker.getOption(Options.WORKER_TASK_KEEPALIVE);

            int keepAlive = undertowTp.getWorkerKeepAlive() * 1000;
            xnioWorker.setOption(Options.WORKER_IO_THREADS, undertowTp.getIoThreads());
            xnioWorker.setOption(Options.WORKER_TASK_CORE_THREADS, undertowTp.getCoreWorkerThreads());
            xnioWorker.setOption(Options.WORKER_TASK_MAX_THREADS, undertowTp.getMaxWorkerThreads());
            xnioWorker.setOption(Options.WORKER_TASK_KEEPALIVE, keepAlive);

            log.info("DynamicTp undertowWebServerTp refreshed end, ioThreads: [{}], " +
                            "coreWorkerThreads: [{}], maxWorkerThreads: [{}], workerThreadKeepAlive: [{}]",
                    String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldIoThreads, undertowTp.getIoThreads()),
                    String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldCoreWorkerThreads, undertowTp.getCoreWorkerThreads()),
                    String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldMaxWorkerThreads, undertowTp.getMaxWorkerThreads()),
                    String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldWorkerKeepAlive, keepAlive));
        } catch (IOException e) {
            log.error("Update undertow web server threadPool failed.", e);
        }
    }

    private XnioWorker convertAndGet() {
        Executor executor = getWebServerTp();
        if (Objects.isNull(executor)) {
            log.warn("Undertow web server threadPool is null.");
            throw new DtpException("Undertow web server threadPool is null.");
        }
        return (XnioWorker) executor;
    }
}
