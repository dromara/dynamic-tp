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
import org.dromara.dynamictp.core.support.adapter.ExecutorAdapter;
import org.dromara.dynamictp.starter.adapter.webserver.AbstractWebServerDtpAdapter;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
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
        TomcatWebServer tomcatWebServer = (TomcatWebServer) webServer;
        Executor originExecutor = tomcatWebServer.getTomcat().getConnector().getProtocolHandler().getExecutor();
        TomcatExecutorProxy proxy = new TomcatExecutorProxy((ThreadPoolExecutor) originExecutor);
        ProtocolHandler protocolHandler = tomcatWebServer.getTomcat().getConnector().getProtocolHandler();
        if (protocolHandler instanceof AbstractProtocol) {
            // compatible with lower version tomcat
            ((AbstractProtocol<?>) protocolHandler).setExecutor(proxy);
            putAndFinalize(getTpName(), (ExecutorService) originExecutor, new TomcatExecutorAdapter(proxy));
        }
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
