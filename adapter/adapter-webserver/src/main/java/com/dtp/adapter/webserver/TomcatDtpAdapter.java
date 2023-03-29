package com.dtp.adapter.webserver;

import com.dtp.common.properties.DtpProperties;
import com.dtp.common.entity.TpExecutorProps;
import com.dtp.common.entity.TpMainFields;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.common.entity.ThreadPoolStats;
import com.dtp.core.convert.ExecutorConverter;
import com.dtp.core.thread.ExecutorAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.WebServer;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.dtp.common.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;

/**
 * TomcatDtpAdapter related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public class TomcatDtpAdapter extends AbstractWebServerDtpAdapter<ThreadPoolExecutor> {

    private static final String POOL_NAME = "tomcatTp";

    @Override
    public ExecutorWrapper doGetExecutorWrapper(WebServer webServer) {
        TomcatWebServer tomcatWebServer = (TomcatWebServer) webServer;
        final TomcatExecutorAdapter adapter = new TomcatExecutorAdapter((ThreadPoolExecutor)
                tomcatWebServer.getTomcat().getConnector().getProtocolHandler().getExecutor());
        return new ExecutorWrapper(POOL_NAME, adapter);
    }

    @Override
    public ThreadPoolStats getPoolStats() {
        ExecutorAdapter<ThreadPoolExecutor> adapter = getExecutor();
        if (Objects.isNull(adapter)) {
            return null;
        }
        ThreadPoolExecutor threadPoolExecutor = adapter.getOriginal();
        return ThreadPoolStats.builder()
                .corePoolSize(threadPoolExecutor.getCorePoolSize())
                .maximumPoolSize(threadPoolExecutor.getMaximumPoolSize())
                .queueType(threadPoolExecutor.getQueue().getClass().getSimpleName())
                .queueCapacity(threadPoolExecutor.getQueue().size() + threadPoolExecutor.getQueue().remainingCapacity())
                .queueSize(threadPoolExecutor.getQueue().size())
                .queueRemainingCapacity(threadPoolExecutor.getQueue().remainingCapacity())
                .activeCount(threadPoolExecutor.getActiveCount())
                .taskCount(threadPoolExecutor.getTaskCount())
                .completedTaskCount(threadPoolExecutor.getCompletedTaskCount())
                .largestPoolSize(threadPoolExecutor.getLargestPoolSize())
                .poolSize(threadPoolExecutor.getPoolSize())
                .waitTaskCount(threadPoolExecutor.getQueue().size())
                .poolName(POOL_NAME)
                .build();
    }

    @Override
    public void refresh(DtpProperties dtpProperties) {
        TpExecutorProps props = dtpProperties.getTomcatTp();
        if (Objects.isNull(props) || containsInvalidParams(props, log)) {
            return;
        }
        ExecutorAdapter<ThreadPoolExecutor> adapter = getExecutor();
        if (Objects.isNull(adapter)) {
            return;
        }

        ThreadPoolExecutor threadPoolExecutor = adapter.getOriginal();
        TpMainFields oldFields = ExecutorConverter.ofSimple(POOL_NAME, threadPoolExecutor.getCorePoolSize(),
                threadPoolExecutor.getMaximumPoolSize(), threadPoolExecutor.getKeepAliveTime(props.getUnit()));
        doRefresh(threadPoolExecutor, props);
        TpMainFields newFields = ExecutorConverter.ofSimple(POOL_NAME, threadPoolExecutor.getCorePoolSize(),
                threadPoolExecutor.getMaximumPoolSize(), threadPoolExecutor.getKeepAliveTime(props.getUnit()));
        if (oldFields.equals(newFields)) {
            log.debug("DynamicTp adapter refresh, main properties of [{}] have not changed.", POOL_NAME);
            return;
        }
        log.info("DynamicTp adapter [{}] refreshed end, corePoolSize: [{}], maxPoolSize: [{}], keepAliveTime: [{}]",
                POOL_NAME,
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldFields.getCorePoolSize(), newFields.getCorePoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldFields.getMaxPoolSize(), newFields.getMaxPoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldFields.getKeepAliveTime(), newFields.getKeepAliveTime()));
    }

    private void doRefresh(ThreadPoolExecutor executor, TpExecutorProps props) {

        if (executor.getKeepAliveTime(props.getUnit()) != props.getKeepAliveTime()) {
            executor.setKeepAliveTime(props.getKeepAliveTime(), props.getUnit());
        }

        int newMaxPoolSize = props.getMaximumPoolSize();
        if (newMaxPoolSize >= executor.getMaximumPoolSize()) {
            if (!Objects.equals(executor.getMaximumPoolSize(), newMaxPoolSize)) {
                executor.setMaximumPoolSize(newMaxPoolSize);
            }
            if (!Objects.equals(executor.getCorePoolSize(), props.getCorePoolSize())) {
                executor.setCorePoolSize(props.getCorePoolSize());
            }
            return;
        }

        if (!Objects.equals(executor.getCorePoolSize(), props.getCorePoolSize())) {
            executor.setCorePoolSize(props.getCorePoolSize());
        }
        if (!Objects.equals(executor.getMaximumPoolSize(), newMaxPoolSize)) {
            executor.setMaximumPoolSize(newMaxPoolSize);
        }
    }
    
    /**
     * TomcatExecutorAdapter implements ExecutorAdapter, the goal of this class
     * is to be compatible with {@link org.apache.tomcat.util.threads.ThreadPoolExecutor}.
     **/
    private static class TomcatExecutorAdapter implements ExecutorAdapter<ThreadPoolExecutor> {
        
        private final ThreadPoolExecutor executor;
        
        public TomcatExecutorAdapter(ThreadPoolExecutor executor) {
            this.executor = executor;
        }
        
        @Override
        public ThreadPoolExecutor getOriginal() {
            return this.executor;
        }
        
        @Override
        public int getCorePoolSize() {
            return this.executor.getCorePoolSize();
        }
        
        @Override
        public void setCorePoolSize(int corePoolSize) {
            this.executor.setCorePoolSize(corePoolSize);
        }
        
        @Override
        public int getMaximumPoolSize() {
            return this.executor.getMaximumPoolSize();
        }
        
        @Override
        public void setMaximumPoolSize(int maximumPoolSize) {
            this.executor.setMaximumPoolSize(maximumPoolSize);
        }
        
        @Override
        public int getPoolSize() {
            return this.executor.getPoolSize();
        }
        
        @Override
        public int getActiveCount() {
            return this.executor.getActiveCount();
        }
        
        @Override
        public int getLargestPoolSize() {
            return this.executor.getLargestPoolSize();
        }
        
        @Override
        public long getTaskCount() {
            return this.executor.getTaskCount();
        }
        
        @Override
        public long getCompletedTaskCount() {
            return this.executor.getCompletedTaskCount();
        }
        
        @Override
        public BlockingQueue<Runnable> getQueue() {
            return this.executor.getQueue();
        }
    
        @Override
        public String getRejectHandlerName() {
            return this.executor.getRejectedExecutionHandler().getClass().getSimpleName();
        }
        
        @Override
        public boolean allowsCoreThreadTimeOut() {
            return this.executor.allowsCoreThreadTimeOut();
        }
        
        @Override
        public void allowCoreThreadTimeOut(boolean value) {
            this.executor.allowCoreThreadTimeOut(value);
        }
        
        @Override
        public long getKeepAliveTime(TimeUnit unit) {
            return this.executor.getKeepAliveTime(unit);
        }
        
        @Override
        public void setKeepAliveTime(long time, TimeUnit unit) {
            this.executor.setKeepAliveTime(time, unit);
        }
    }
}
