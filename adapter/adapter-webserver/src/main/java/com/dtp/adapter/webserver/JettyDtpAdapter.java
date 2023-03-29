package com.dtp.adapter.webserver;

import com.dtp.common.properties.DtpProperties;
import com.dtp.common.entity.TpExecutorProps;
import com.dtp.common.entity.TpMainFields;
import com.dtp.common.util.ReflectionUtil;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.common.entity.ThreadPoolStats;
import com.dtp.core.convert.ExecutorConverter;
import com.dtp.core.thread.ExecutorAdapter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.util.thread.MonitoredQueuedThreadPool;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.springframework.boot.web.embedded.jetty.JettyWebServer;
import org.springframework.boot.web.server.WebServer;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;

import static com.dtp.common.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;

/**
 * JettyDtpAdapter related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public class JettyDtpAdapter extends AbstractWebServerDtpAdapter<ThreadPool.SizedThreadPool> {

    private static final String POOL_NAME = "jettyTp";

    @Override
    public ExecutorWrapper doGetExecutorWrapper(WebServer webServer) {
        JettyWebServer jettyWebServer = (JettyWebServer) webServer;
        final JettyExecutorAdapter adapter = new JettyExecutorAdapter(
                (ThreadPool.SizedThreadPool) jettyWebServer.getServer().getThreadPool());
        return new ExecutorWrapper(POOL_NAME, adapter);
    }

    @Override
    public ThreadPoolStats getPoolStats() {

        ExecutorAdapter<ThreadPool.SizedThreadPool> adapter = getExecutor();
        if (Objects.isNull(adapter)) {
            return null;
        }

        ThreadPool.SizedThreadPool threadPool = adapter.getOriginal();
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
        ExecutorAdapter<ThreadPool.SizedThreadPool> adapter = getExecutor();
        if (Objects.isNull(adapter)) {
            return;
        }

        ThreadPool.SizedThreadPool threadPool = adapter.getOriginal();
        int oldCoreSize = threadPool.getMinThreads();
        int oldMaxSize = threadPool.getMaxThreads();
        TpMainFields oldFields = ExecutorConverter.ofSimple(POOL_NAME, oldCoreSize, oldMaxSize, 0L);
        doRefresh(threadPool, props);
        TpMainFields newFields = ExecutorConverter.ofSimple(props.getThreadPoolName(), threadPool.getMinThreads(),
                threadPool.getMaxThreads(), 0L);
        if (oldFields.equals(newFields)) {
            log.debug("DynamicTp adapter refresh, main properties of [{}] have not changed.", POOL_NAME);
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
    
    /**
     * JettyExecutorAdapter implements ExecutorAdapter, the goal of this class
     * is to be compatible with {@link org.eclipse.jetty.util.thread.ThreadPool.SizedThreadPool}.
     **/
    private static class JettyExecutorAdapter implements ExecutorAdapter<ThreadPool.SizedThreadPool> {
        
        private final ThreadPool.SizedThreadPool executor;
        
        public JettyExecutorAdapter(ThreadPool.SizedThreadPool executor) {
            this.executor = executor;
        }
        
        @Override
        public ThreadPool.SizedThreadPool getOriginal() {
            return this.executor;
        }
        
        @Override
        public int getCorePoolSize() {
            return this.executor.getMinThreads();
        }
        
        @Override
        public void setCorePoolSize(int corePoolSize) {
            this.executor.setMinThreads(corePoolSize);
        }
        
        @Override
        public int getMaximumPoolSize() {
            return this.executor.getMaxThreads();
        }
        
        @Override
        public void setMaximumPoolSize(int maximumPoolSize) {
            this.executor.setMaxThreads(maximumPoolSize);
        }
        
        @Override
        public int getPoolSize() {
            return this.executor.getThreads();
        }
        
        @Override
        public int getActiveCount() {
            if (this.executor instanceof QueuedThreadPool) {
                return ((QueuedThreadPool) this.executor).getBusyThreads();
            }
            return -1;
        }
        
        @Override
        public int getLargestPoolSize() {
            if (this.executor instanceof MonitoredQueuedThreadPool) {
                return ((MonitoredQueuedThreadPool) this.executor).getMaxBusyThreads();
            }
            return -1;
        }
        
        @Override
        public long getCompletedTaskCount() {
            if (this.executor instanceof MonitoredQueuedThreadPool) {
                return ((MonitoredQueuedThreadPool) this.executor).getTasks();
            }
            return -1;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public BlockingQueue<Runnable> getQueue() {
            return (BlockingQueue<Runnable>) ReflectionUtil.getFieldValue(QueuedThreadPool.class, "_jobs", this.executor);
        }
    }
}
