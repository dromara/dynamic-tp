package com.dtp.adapter.webserver;

import com.dtp.common.entity.ThreadPoolStats;
import com.dtp.common.entity.TpExecutorProps;
import com.dtp.common.entity.TpMainFields;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.util.ReflectionUtil;
import com.dtp.core.convert.ExecutorConverter;
import com.dtp.core.support.ExecutorWrapper;
import io.undertow.Undertow;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServer;
import org.springframework.boot.web.server.WebServer;
import org.xnio.Options;
import org.xnio.XnioWorker;
import org.xnio.management.XnioWorkerMXBean;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executor;

import static com.dtp.common.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;

/**
 * UndertowDtpAdapter related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public class UndertowDtpAdapter extends AbstractWebServerDtpAdapter {

    private static final String POOL_NAME = "undertowTp";

    public UndertowDtpAdapter() {
        super();
        System.setProperty("jboss.threads.eqe.statistics.active-count", "true");
    }

    @Override
    public ExecutorWrapper doGetExecutorWrapper(WebServer webServer) {

        UndertowServletWebServer undertowServletWebServer = (UndertowServletWebServer) webServer;
        val undertow = (Undertow) ReflectionUtil.getFieldValue(UndertowServletWebServer.class,
                "undertow", undertowServletWebServer);
        if (Objects.isNull(undertow)) {
            return null;
        }
        return new ExecutorWrapper(POOL_NAME, undertow.getWorker());
    }

    @Override
    public ThreadPoolStats getPoolStats() {

        Executor executor = getExecutor();
        if (Objects.isNull(executor)) {
            return null;
        }
        XnioWorker xnioWorker = (XnioWorker) executor;
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
        TpExecutorProps props = dtpProperties.getUndertowTp();
        if (Objects.isNull(props) || containsInvalidParams(props, log)) {
            return;
        }
        Executor executor = getExecutor();
        if (Objects.isNull(executor)) {
            return;
        }

        XnioWorker xnioWorker = (XnioWorker) executor;
        try {
            int oldCorePoolSize = xnioWorker.getOption(Options.WORKER_TASK_CORE_THREADS);
            int oldMaxPoolSize = xnioWorker.getOption(Options.WORKER_TASK_MAX_THREADS);
            int oldKeepAliveTime = xnioWorker.getOption(Options.WORKER_TASK_KEEPALIVE);
            TpMainFields oldFields = ExecutorConverter.ofSimple(props.getThreadPoolName(), oldCorePoolSize,
                    oldMaxPoolSize, oldKeepAliveTime);
            doRefresh(xnioWorker, props);

            int newCorePoolSize = xnioWorker.getOption(Options.WORKER_TASK_CORE_THREADS);
            int newMaxPoolSize = xnioWorker.getOption(Options.WORKER_TASK_MAX_THREADS);
            int newKeepAliveTime = xnioWorker.getOption(Options.WORKER_TASK_KEEPALIVE);
            TpMainFields newFields = ExecutorConverter.ofSimple(props.getThreadPoolName(), newCorePoolSize,
                    newMaxPoolSize, newKeepAliveTime);
            if (oldFields.equals(newFields)) {
                log.debug("DynamicTp adapter refresh, main properties of [{}] have not changed.", POOL_NAME);
                return;
            }

            log.info("DynamicTp adapter [{}] refreshed end, corePoolSize: [{}], maxPoolSize: [{}], keepAliveTime: [{}]",
                    POOL_NAME,
                    String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldCorePoolSize, newCorePoolSize),
                    String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldMaxPoolSize, newMaxPoolSize),
                    String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldKeepAliveTime, newKeepAliveTime));
        } catch (IOException e) {
            log.error("Refresh undertow web server threadPool failed.", e);
        }
    }

    private void doRefresh(XnioWorker xnioWorker, TpExecutorProps props) {

        try {
            int keepAlive = (int) props.getKeepAliveTime() * 1000;
            if (!Objects.equals(xnioWorker.getOption(Options.WORKER_TASK_KEEPALIVE), keepAlive)) {
                xnioWorker.setOption(Options.WORKER_TASK_KEEPALIVE, keepAlive);
            }

            if (props.getMaximumPoolSize() < xnioWorker.getOption(Options.WORKER_TASK_MAX_THREADS)) {
                if (!Objects.equals(xnioWorker.getOption(Options.WORKER_TASK_CORE_THREADS), props.getCorePoolSize())) {
                    xnioWorker.setOption(Options.WORKER_TASK_CORE_THREADS, props.getCorePoolSize());
                }
                if (!Objects.equals(xnioWorker.getOption(Options.WORKER_TASK_MAX_THREADS), props.getMaximumPoolSize())) {
                    xnioWorker.setOption(Options.WORKER_TASK_MAX_THREADS, props.getMaximumPoolSize());
                }
                return;
            }

            if (!Objects.equals(xnioWorker.getOption(Options.WORKER_TASK_MAX_THREADS), props.getMaximumPoolSize())) {
                xnioWorker.setOption(Options.WORKER_TASK_MAX_THREADS, props.getMaximumPoolSize());
            }
            if (!Objects.equals(xnioWorker.getOption(Options.WORKER_TASK_CORE_THREADS), props.getCorePoolSize())) {
                xnioWorker.setOption(Options.WORKER_TASK_CORE_THREADS, props.getCorePoolSize());
            }
        } catch (IOException e) {
            log.error("Update undertow web server threadPool failed.", e);
        }
    }
}
