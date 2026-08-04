/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.starter.adapter.webserver.tomcat;

import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.aware.RejectHandlerAware;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.adapter.ExecutorAdapter;
import org.dromara.dynamictp.core.support.proxy.VirtualThreadExecutorProxy;
import org.dromara.dynamictp.starter.adapter.webserver.AbstractWebServerDtpAdapter;
import org.springframework.boot.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.WebServer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TomcatDtpAdapter related
 *
 * @author yanhom
 * @author dragon-zhang
 * @since 1.0.0
 */
@Slf4j
public class TomcatDtpAdapter extends AbstractWebServerDtpAdapter<Executor> {

    private static final String TP_PREFIX = "tomcatTp";

    @Override
    public void doEnhance(WebServer webServer) {
        if (!(webServer instanceof TomcatWebServer tomcatWebServer)) {
            log.warn("DynamicTp adapter, skip tomcat enhance, unexpected webServer type: {}.",
                    webServer == null ? "null" : webServer.getClass().getName());
            return;
        }
        ProtocolHandler protocolHandler = tomcatWebServer.getTomcat().getConnector().getProtocolHandler();
        if (!(protocolHandler instanceof AbstractProtocol)) {
            log.warn("DynamicTp adapter, skip tomcat enhance, protocolHandler type: {}.",
                    protocolHandler == null ? "null" : protocolHandler.getClass().getName());
            return;
        }
        AbstractProtocol<?> protocol = (AbstractProtocol<?>) protocolHandler;
        Executor originExecutor = protocol.getExecutor();
        if (originExecutor instanceof ThreadPoolExecutor) {
            enhancePlatformThreadPool(protocol, (ThreadPoolExecutor) originExecutor);
            return;
        }
        // server.tomcat.threads.virtual.enabled=true → VirtualThreadExecutor
        if (originExecutor instanceof ExecutorService) {
            enhanceVirtualThreadExecutor(protocol, (ExecutorService) originExecutor);
            return;
        }
        log.warn("DynamicTp adapter, skip tomcat enhance, unsupported executor type: {}.",
                originExecutor == null ? "null" : originExecutor.getClass().getName());
    }

    /**
     * Classic Tomcat platform-thread pool: replace with {@link TomcatExecutorProxy}
     * and shut down the abandoned original pool.
     */
    private void enhancePlatformThreadPool(AbstractProtocol<?> protocol, ThreadPoolExecutor origin) {
        TomcatExecutorProxy proxy = new TomcatExecutorProxy(origin);
        protocol.setExecutor(proxy);
        putAndFinalize(getTpName(), origin, new TomcatExecutorAdapter(proxy));
    }

    /**
     * Tomcat virtual-thread executor: wrap (do not replace/shutdown) the original
     * so task wrappers / aware still apply. Pool-size metrics stay unsupported.
     */
    private void enhanceVirtualThreadExecutor(AbstractProtocol<?> protocol, ExecutorService origin) {
        log.info("DynamicTp adapter, tomcatTp detected virtual-thread executor ({}), "
                        + "wrapping with VirtualThreadExecutorProxy (pool-size refresh not applicable).",
                origin.getClass().getName());
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(origin);
        proxy.setThreadPoolName(getTpName());
        protocol.setExecutor(proxy);
        // Must not shutdown origin: proxy still delegates to it.
        executors.put(getTpName(), new ExecutorWrapper(getTpName(), proxy));
    }

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(executors.get(getTpName()), dtpProperties.getPlatforms(), dtpProperties.getTomcatTp());
    }

    @Override
    protected String getTpPrefix() {
        return TP_PREFIX;
    }

    /**
     * TomcatExecutorAdapter implements ExecutorAdapter, the goal of this class
     * is to be compatible with {@link org.apache.tomcat.util.threads.ThreadPoolExecutor}.
     **/
    private static class TomcatExecutorAdapter implements ExecutorAdapter<Executor> {
        
        private final Executor executor;
        
        TomcatExecutorAdapter(Executor executor) {
            this.executor = executor;
        }
        
        @Override
        public Executor getOriginal() {
            return this.executor;
        }

        public ThreadPoolExecutor getTomcatExecutor() {
            return (ThreadPoolExecutor) this.executor;
        }
        
        @Override
        public int getCorePoolSize() {
            return getTomcatExecutor().getCorePoolSize();
        }
        
        @Override
        public void setCorePoolSize(int corePoolSize) {
            getTomcatExecutor().setCorePoolSize(corePoolSize);
        }
        
        @Override
        public int getMaximumPoolSize() {
            return getTomcatExecutor().getMaximumPoolSize();
        }
        
        @Override
        public void setMaximumPoolSize(int maximumPoolSize) {
            getTomcatExecutor().setMaximumPoolSize(maximumPoolSize);
        }
        
        @Override
        public int getPoolSize() {
            return getTomcatExecutor().getPoolSize();
        }
        
        @Override
        public int getActiveCount() {
            return getTomcatExecutor().getActiveCount();
        }
        
        @Override
        public int getLargestPoolSize() {
            return getTomcatExecutor().getLargestPoolSize();
        }
        
        @Override
        public long getTaskCount() {
            return getTomcatExecutor().getTaskCount();
        }
        
        @Override
        public long getCompletedTaskCount() {
            return getTomcatExecutor().getCompletedTaskCount();
        }
        
        @Override
        public BlockingQueue<Runnable> getQueue() {
            return getTomcatExecutor().getQueue();
        }
    
        @Override
        public String getRejectHandlerType() {
            return ((RejectHandlerAware) getTomcatExecutor()).getRejectHandlerType();
        }
        
        @Override
        public boolean allowsCoreThreadTimeOut() {
            return getTomcatExecutor().allowsCoreThreadTimeOut();
        }
        
        @Override
        public void allowCoreThreadTimeOut(boolean value) {
            getTomcatExecutor().allowCoreThreadTimeOut(value);
        }

        @Override
        public void preStartAllCoreThreads() {
            getTomcatExecutor().prestartAllCoreThreads();
        }

        @Override
        public long getKeepAliveTime(TimeUnit unit) {
            return getTomcatExecutor().getKeepAliveTime(unit);
        }
        
        @Override
        public void setKeepAliveTime(long time, TimeUnit unit) {
            getTomcatExecutor().setKeepAliveTime(time, unit);
        }

        @Override
        public boolean isShutdown() {
            return getTomcatExecutor().isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return getTomcatExecutor().isTerminated();
        }

        @Override
        public boolean isTerminating() {
            return getTomcatExecutor().isTerminating();
        }

    }
}
