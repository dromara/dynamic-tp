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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.jetty.InstrumentedQueuedThreadPool;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.support.ExecutorAdapter;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolExecutorProxy;
import org.dromara.dynamictp.starter.adapter.webserver.AbstractWebServerDtpAdapter;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.eclipse.jetty.util.thread.MonitoredQueuedThreadPool;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.springframework.boot.web.embedded.jetty.JettyWebServer;
import org.springframework.boot.web.server.WebServer;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
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

    private static final String TP_PREFIX = "jettyTp";

    private static final String EXECUTOR_FIELD = "_executor";

    private static final String THREAD_POOL_FIELD = "_threadPool";

    @Override
    public ExecutorWrapper enhanceAndGetExecutorWrapper(WebServer webServer) {
        JettyWebServer jettyWebServer = (JettyWebServer) webServer;
        ThreadPool threadPool = jettyWebServer.getServer().getThreadPool();
        Executor executorProxy = enhanceOriginExecutor(jettyWebServer, threadPool);
        ExecutorWrapper wrapper;
        if (executorProxy instanceof ThreadPoolExecutor) {
            wrapper = new ExecutorWrapper(TP_PREFIX, executorProxy);
        } else {
            final JettyExecutorAdapter adapter = new JettyExecutorAdapter(
                    (ThreadPool.SizedThreadPool) executorProxy);
            wrapper = new ExecutorWrapper(TP_PREFIX, adapter);
        }
        return wrapper;
    }

    private Executor enhanceOriginExecutor(JettyWebServer webServer, ThreadPool threadPool) {
        try {
            if (threadPool instanceof ExecutorThreadPool) {
                val executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(EXECUTOR_FIELD, threadPool);
                if (Objects.isNull(executor)) {
                    return threadPool;
                }
                ThreadPoolExecutorProxy executorProxy = new ThreadPoolExecutorProxy(executor);
                ReflectionUtil.setFieldValue(EXECUTOR_FIELD, threadPool, executorProxy);
                return executorProxy;
            }
            Object threadPoolProxy = createThreadPoolProxy(threadPool);
            if (!(threadPoolProxy instanceof QueuedThreadPoolProxy)) {
                ReflectionUtil.setFieldValue(THREAD_POOL_FIELD, webServer.getServer(), threadPoolProxy);
            }
            return (Executor) threadPoolProxy;
        } catch (IllegalAccessException e) {
            log.error("DynamicTp enhance jetty origin executor failed.", e);
        }
        return threadPool;
    }

    private Object createThreadPoolProxy(ThreadPool threadPool) {
        if (!(threadPool instanceof QueuedThreadPool)) {
            return threadPool;
        }

        QueuedThreadPool queuedThreadPool = (QueuedThreadPool) threadPool;
        BlockingQueue<Runnable> queue = cast(ReflectionUtil.getFieldValue("_jobs", threadPool));
        if (threadPool instanceof InstrumentedQueuedThreadPool) {
            MeterRegistry registry = (MeterRegistry) ReflectionUtil.getFieldValue("registry", threadPool);
            Iterable<Tag> tags = cast(ReflectionUtil.getFieldValue("tags", threadPool));
            return new InstrumentedQueuedThreadPoolProxy(queuedThreadPool, registry, tags, queue);
        } else if (threadPool instanceof MonitoredQueuedThreadPool) {
            return new MonitoredQueuedThreadPoolProxy(queuedThreadPool, queue);
        } else {
            val threadGroup = (ThreadGroup) ReflectionUtil.getFieldValue("_threadGroup", threadPool);
            val threadFactory = (ThreadFactory) ReflectionUtil.getFieldValue("_threadFactory", threadPool);
            return new QueuedThreadPoolProxy(queuedThreadPool, queue, threadGroup, threadFactory);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T cast(Object obj) {
        return (T) obj;
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
