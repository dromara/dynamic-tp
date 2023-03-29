package com.dtp.adapter.webserver;

import com.dtp.common.entity.ThreadPoolStats;
import com.dtp.common.entity.TpExecutorProps;
import com.dtp.common.entity.TpMainFields;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.util.ReflectionUtil;
import com.dtp.core.convert.ExecutorConverter;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.thread.ExecutorAdapter;
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
import java.util.concurrent.TimeUnit;

import static com.dtp.common.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;

/**
 * UndertowDtpAdapter related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public class UndertowDtpAdapter extends AbstractWebServerDtpAdapter<XnioWorker> {

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
        final UndertowExecutorAdapter adapter = new UndertowExecutorAdapter(undertow.getWorker());
        return new ExecutorWrapper(POOL_NAME, adapter);
    }

    @Override
    public ThreadPoolStats getPoolStats() {

        ExecutorAdapter<XnioWorker> adapter = getExecutor();
        if (Objects.isNull(adapter)) {
            return null;
        }
        XnioWorkerMXBean mxBean = adapter.getOriginal().getMXBean();
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
        ExecutorAdapter<XnioWorker> executor = getExecutor();
        if (Objects.isNull(executor)) {
            return;
        }

        XnioWorker xnioWorker = executor.getOriginal();
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
    
    /**
     * UndertowExecutorAdapter implements ExecutorAdapter, the goal of this class
     * is to be compatible with {@link org.xnio.XnioWorker}.
     **/
    private static class UndertowExecutorAdapter implements ExecutorAdapter<XnioWorker> {
        
        private final XnioWorker executor;
        
        public UndertowExecutorAdapter(XnioWorker executor) {
            this.executor = executor;
        }
        
        @Override
        public XnioWorker getOriginal() {
            return this.executor;
        }
        
        @Override
        public int getCorePoolSize() {
            try {
                return this.executor.getOption(Options.WORKER_TASK_CORE_THREADS);
            } catch (IOException e) {
                log.error("getCorePoolSize from undertow web server threadPool failed.", e);
                return this.executor.getMXBean().getCoreWorkerPoolSize();
            }
        }
        
        @Override
        public void setCorePoolSize(int corePoolSize) {
            try {
                this.executor.setOption(Options.WORKER_TASK_CORE_THREADS, corePoolSize);
            } catch (IOException e) {
                log.error("Update undertow web server threadPool CorePoolSize failed.", e);
            }
        }
        
        @Override
        public int getMaximumPoolSize() {
            try {
                return this.executor.getOption(Options.WORKER_TASK_MAX_THREADS);
            } catch (IOException e) {
                log.error("getMaximumPoolSize from undertow web server threadPool failed.", e);
                return this.executor.getMXBean().getMaxWorkerPoolSize();
            }
        }
        
        @Override
        public void setMaximumPoolSize(int maximumPoolSize) {
            try {
                this.executor.setOption(Options.WORKER_TASK_MAX_THREADS, maximumPoolSize);
            } catch (IOException e) {
                log.error("Update undertow web server threadPool MaximumPoolSize failed.", e);
            }
        }
        
        @Override
        public int getPoolSize() {
            return this.executor.getMXBean().getWorkerPoolSize();
        }
        
        @Override
        public int getActiveCount() {
            return this.executor.getMXBean().getBusyWorkerThreadCount();
        }
        
        @Override
        public long getKeepAliveTime(TimeUnit unit) {
            try {
                return unit.convert(this.executor.getOption(Options.WORKER_TASK_KEEPALIVE), TimeUnit.MILLISECONDS);
            } catch (IOException e) {
                log.error("getKeepAliveTime from undertow web server threadPool failed.", e);
                return -1;
            }
        }
        
        @Override
        public void setKeepAliveTime(long time, TimeUnit unit) {
            try {
                int keepAlive = (int) TimeUnit.MILLISECONDS.convert(time, unit);
                this.executor.setOption(Options.WORKER_TASK_KEEPALIVE, keepAlive);
            } catch (IOException e) {
                log.error("Update undertow web server threadPool KeepAliveTime failed.", e);
            }
        }
    }
}
