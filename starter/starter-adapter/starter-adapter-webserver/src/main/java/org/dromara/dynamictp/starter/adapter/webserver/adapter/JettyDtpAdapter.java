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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.jetty.InstrumentedQueuedThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.support.ExecutorAdapter;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolExecutorProxy;
import org.dromara.dynamictp.starter.adapter.webserver.adapter.proxy.InstrumentedQueuedThreadPoolProxy;
import org.dromara.dynamictp.starter.adapter.webserver.adapter.proxy.MonitoredQueuedThreadPoolProxy;
import org.dromara.dynamictp.starter.adapter.webserver.adapter.proxy.QueuedThreadPoolProxy;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.eclipse.jetty.util.thread.MonitoredQueuedThreadPool;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.springframework.boot.web.embedded.jetty.JettyWebServer;
import org.springframework.boot.web.server.WebServer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
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

    private static final String POOL_NAME = "jettyTp";

    private static final String EXECUTOR_NAME = "_executor";

    private static final String THREAD_POOL_NAME = "_threadPool";

    @Override
    public ExecutorWrapper doInitExecutorWrapper(WebServer webServer) {
        JettyWebServer jettyWebServer = (JettyWebServer) webServer;
        ThreadPool threadPool = jettyWebServer.getServer().getThreadPool();
        final JettyExecutorAdapter adapter = new JettyExecutorAdapter(
                (ThreadPool.SizedThreadPool) threadPool);
        ExecutorWrapper executorWrapper = new ExecutorWrapper(POOL_NAME, adapter);
        replaceOriginExecutor(jettyWebServer, threadPool);
        return executorWrapper;
    }

    private void replaceOriginExecutor(JettyWebServer jettyWebServer, ThreadPool threadPool) {
        try {
            if (threadPool instanceof ExecutorThreadPool) {
                ThreadPoolExecutor executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(ExecutorThreadPool.class, EXECUTOR_NAME, threadPool);
                ThreadPoolExecutor proxy = new ThreadPoolExecutorProxy(new ExecutorWrapper(POOL_NAME + EXECUTOR_NAME, executor));
                ReflectionUtil.setFieldValue(ExecutorThreadPool.class, EXECUTOR_NAME, threadPool, proxy);
            } else {
                Object jettyDtpInterceptor = createThreadPoolPlugin(threadPool);
                if (!(jettyDtpInterceptor instanceof QueuedThreadPoolProxy)) {
                    ReflectionUtil.setFieldValue(Server.class, THREAD_POOL_NAME, jettyWebServer.getServer(), jettyDtpInterceptor);
                }
            }
        } catch (IllegalAccessException e) {
            log.error("Jetty executor proxy exception", e);
        }
    }

    private Object createThreadPoolPlugin(ThreadPool threadPool) {
        if (!(threadPool instanceof QueuedThreadPool)) {
            return threadPool;
        }

        QueuedThreadPool queuedThreadPool = (QueuedThreadPool) threadPool;
        int maxThreads = queuedThreadPool.getMaxThreads();
        int minThreads = queuedThreadPool.getMinThreads();
        int idleTimeout = queuedThreadPool.getIdleTimeout();
        int reservedThreads = queuedThreadPool.getReservedThreads();
        BlockingQueue<Runnable> queue = cast(ReflectionUtil.getFieldValue(QueuedThreadPool.class, "_jobs", threadPool));
        ThreadGroup threadGroup = (ThreadGroup) ReflectionUtil.getFieldValue(QueuedThreadPool.class, "_threadGroup", threadPool);
        ThreadFactory threadFactory = (ThreadFactory) ReflectionUtil.getFieldValue(QueuedThreadPool.class, "_threadFactory", threadPool);

        if (threadPool instanceof InstrumentedQueuedThreadPool) {
            MeterRegistry registry = (MeterRegistry) ReflectionUtil.getFieldValue(InstrumentedQueuedThreadPool.class, "registry", threadPool);
            Iterable<Tag> tags = cast(ReflectionUtil.getFieldValue(InstrumentedQueuedThreadPool.class, "tags", threadPool));
            return new InstrumentedQueuedThreadPoolProxy(
                    (InstrumentedQueuedThreadPool) threadPool,
                    registry,
                    tags,
                    maxThreads,
                    minThreads,
                    idleTimeout,
                    queue);
        } else if (threadPool instanceof MonitoredQueuedThreadPool) {
            return new MonitoredQueuedThreadPoolProxy(
                    (MonitoredQueuedThreadPool) threadPool,
                    maxThreads,
                    minThreads,
                    idleTimeout,
                    queue);
        } else {
            return new QueuedThreadPoolProxy(
                    (QueuedThreadPool) threadPool,
                    maxThreads,
                    minThreads,
                    idleTimeout,
                    reservedThreads,
                    queue,
                    threadGroup,
                    threadFactory);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T cast(Object obj) {
        return (T) obj;
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
