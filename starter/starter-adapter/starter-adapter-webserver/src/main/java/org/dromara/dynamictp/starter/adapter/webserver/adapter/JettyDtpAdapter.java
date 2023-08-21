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

package org.dromara.dynamictp.starter.adapter.webserver.adapter;

import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.plugin.DtpInterceptorRegistry;
import org.dromara.dynamictp.core.support.ThreadPoolExecutorProxy;
import org.dromara.dynamictp.core.support.ExecutorAdapter;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.starter.adapter.webserver.adapter.proxy.JettyDtpInterceptor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.eclipse.jetty.util.thread.MonitoredQueuedThreadPool;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.springframework.boot.web.embedded.jetty.JettyWebServer;
import org.springframework.boot.web.server.WebServer;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * JettyDtpAdapter related
 * <p>
 * Other thread pools are brokered by {@link JettyDtpInterceptor}
 *
 * @author yanhom
 * @author dragon-zhang
 * @since 1.0.0
 */
@Slf4j
public class JettyDtpAdapter extends AbstractWebServerDtpAdapter<ThreadPool.SizedThreadPool> {

    private static final String POOL_NAME = "jettyTp";

    private static final String EXECUTOR_NAME = "_executor";

    private static final String THREAD_POOL_NAME = "_threadPool";

    private static final String JETTY_DTP_INTERCEPTOR_NAME = "jettyDtpInterceptor";

    @Override
    public ExecutorWrapper doInitExecutorWrapper(WebServer webServer) {
        JettyWebServer jettyWebServer = (JettyWebServer) webServer;
        ThreadPool threadPool = jettyWebServer.getServer().getThreadPool();
        final JettyExecutorAdapter adapter = new JettyExecutorAdapter(
                (ThreadPool.SizedThreadPool) threadPool);
        ExecutorWrapper executorWrapper = new ExecutorWrapper(POOL_NAME, adapter);

        try {
            if (threadPool instanceof ExecutorThreadPool) {
                ThreadPoolExecutor executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(ExecutorThreadPool.class, EXECUTOR_NAME, threadPool);
                ThreadPoolExecutor proxy = new ThreadPoolExecutorProxy(new ExecutorWrapper(POOL_NAME + EXECUTOR_NAME, executor));
                ReflectionUtil.setFieldValue(ExecutorThreadPool.class, EXECUTOR_NAME, threadPool, proxy);
            } else {
                DtpInterceptorRegistry.register(JETTY_DTP_INTERCEPTOR_NAME, new JettyDtpInterceptor());
                Object jettyDtpInterceptor = DtpInterceptorRegistry.plugin(threadPool, new HashSet<>(Collections.singletonList(JETTY_DTP_INTERCEPTOR_NAME)));
                ReflectionUtil.setFieldValue(Server.class, THREAD_POOL_NAME, jettyWebServer.getServer(), jettyDtpInterceptor);
            }
        } catch (IllegalAccessException e) {
            log.error("Jetty executor proxy exception", e);
        }
        return executorWrapper;
    }

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(POOL_NAME, executors.get(getTpName()), dtpProperties.getPlatforms(), dtpProperties.getJettyTp());
    }

    @Override
    protected String getTpName() {
        return POOL_NAME;
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
