package com.dtp.adapter.webserver;

import com.dtp.common.entity.ThreadPoolStats;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.util.ReflectionUtil;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.support.ExecutorAdapter;
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

/**
 * UndertowDtpAdapter related
 *
 * @author yanhom
 * @author dragon-zhang
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
        refresh(POOL_NAME, executorWrapper, dtpProperties.getPlatforms(), dtpProperties.getUndertowTp());
    }
    
    /**
     * UndertowExecutorAdapter implements ExecutorAdapter, the goal of this class
     * is to be compatible with {@link org.xnio.XnioWorker}.
     **/
    private static class UndertowExecutorAdapter implements ExecutorAdapter<XnioWorker> {
        
        private final XnioWorker executor;
        
        UndertowExecutorAdapter(XnioWorker executor) {
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
