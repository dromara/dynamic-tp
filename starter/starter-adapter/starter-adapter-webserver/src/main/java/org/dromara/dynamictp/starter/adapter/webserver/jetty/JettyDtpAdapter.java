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

package org.dromara.dynamictp.starter.adapter.webserver.jetty;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.support.adapter.ExecutorAdapter;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.starter.adapter.webserver.AbstractWebServerDtpAdapter;
import org.eclipse.jetty.io.ManagedSelector;
import org.eclipse.jetty.io.SelectorManager;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.ExecutionStrategy;
import org.eclipse.jetty.util.thread.MonitoredQueuedThreadPool;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.util.thread.strategy.EatWhatYouKill;
import org.springframework.boot.web.embedded.jetty.JettyWebServer;
import org.springframework.boot.web.server.WebServer;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * JettyDtpAdapter related
 *
 * @author yanhom
 * @author dragon-zhang
 * @since 1.0.0
 */
@Slf4j
public class JettyDtpAdapter extends AbstractWebServerDtpAdapter<ThreadPool.SizedThreadPool> {

    private static final String TP_PREFIX = "jettyTp";

    private static final String CONNECTORS_FIELD = "connectors";

    private static final String MANAGER_FIELD = "_manager";

    private static final String SELECTORS_FIELD = "_selectors";

    private static final String STRATEGY_FIELD = "_strategy";

    private static final String PRODUCER_FIELD = "_producer";

    @Override
    public void doEnhance(WebServer webServer) {
        JettyWebServer jettyWebServer = (JettyWebServer) webServer;
        ThreadPool threadPool = jettyWebServer.getServer().getThreadPool();
        JettyExecutorAdapter adapter = new JettyExecutorAdapter((ThreadPool.SizedThreadPool) threadPool);
        enhanceOriginTask(jettyWebServer, threadPool);
        String tpName = getTpName();
        executors.put(tpName, new ExecutorWrapper(tpName, adapter));
    }

    private void enhanceOriginTask(JettyWebServer jettyWebServer, ThreadPool threadPool) {
        Connector[] connectors = (Connector[]) ReflectionUtil.getFieldValue(CONNECTORS_FIELD, jettyWebServer);
        if (Objects.isNull(connectors)) {
            return;
        }
        for (Connector connector : connectors) {
            if (!(connector instanceof ServerConnector)) {
                continue;
            }
            SelectorManager selectorManager = (SelectorManager) ReflectionUtil.getFieldValue(MANAGER_FIELD, connector);
            if (Objects.isNull(selectorManager)) {
                return;
            }
            ManagedSelector[] managedSelectors = (ManagedSelector[]) ReflectionUtil.getFieldValue(SELECTORS_FIELD, selectorManager);
            if (Objects.isNull(managedSelectors)) {
                return;
            }
            for (ManagedSelector managedSelector : managedSelectors) {
                EatWhatYouKill eatWhatYouKill = (EatWhatYouKill) ReflectionUtil.getFieldValue(STRATEGY_FIELD, managedSelector);
                if (Objects.isNull(eatWhatYouKill)) {
                    continue;
                }
                ExecutionStrategy.Producer producer = (ExecutionStrategy.Producer) ReflectionUtil.getFieldValue(PRODUCER_FIELD, eatWhatYouKill);
                SelectorProducerProxy selectorProducerProxy = new SelectorProducerProxy(producer, threadPool);
                ReflectionUtil.setFieldValue(PRODUCER_FIELD, eatWhatYouKill, selectorProducerProxy);
            }
        }
    }

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(executors.get(getTpName()), dtpProperties.getPlatforms(), dtpProperties.getJettyTp());
    }

    @Override
    protected String getTpPrefix() {
        return TP_PREFIX;
    }

    /**
     * JettyExecutorAdapter implements ExecutorAdapter, the goal of this class
     * is to be compatible with {@link org.eclipse.jetty.util.thread.ThreadPool.SizedThreadPool}.
     **/
    private static class JettyExecutorAdapter implements ExecutorAdapter<ThreadPool.SizedThreadPool> {

        private final ThreadPool.SizedThreadPool executor;

        JettyExecutorAdapter(ThreadPool.SizedThreadPool executor) {
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

        @Override
        public long getKeepAliveTime(TimeUnit unit) {
            if (this.executor instanceof QueuedThreadPool) {
                return ((QueuedThreadPool) this.executor).getIdleTimeout();
            }
            return 0;
        }
    }
}
