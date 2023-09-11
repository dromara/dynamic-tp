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

package org.dromara.dynamictp.starter.adapter.webserver.adapter.jetty;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.support.task.runnable.EnhancedRunnable;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ReservedThreadExecutor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;

/**
 * @author hanli
 * @since 1.1.4
 */
@Slf4j
public class QueuedThreadPoolProxy extends QueuedThreadPool {

    private final QueuedThreadPool original;

    public QueuedThreadPoolProxy(QueuedThreadPool threadPool, BlockingQueue<Runnable> queue,
                                 ThreadGroup threadGroup, ThreadFactory threadFactory) {
        super(threadPool.getMaxThreads(), threadPool.getMinThreads(), threadPool.getIdleTimeout(),
                threadPool.getReservedThreads(), queue, threadGroup, threadFactory);
        this.original = threadPool;
        try {
            Object counts = ReflectionUtil.getFieldValue("_counts", original);
            ReflectionUtil.setFieldValue("_counts", this, counts);
            Object tryExecutor = ReflectionUtil.getFieldValue("_tryExecutor", original);
            if (tryExecutor instanceof ReservedThreadExecutor) {
                ReservedThreadExecutor rtExecutor = (ReservedThreadExecutor) tryExecutor;
                if (rtExecutor.getExecutor() == original) {
                    ReflectionUtil.setFieldValue("_executor", rtExecutor, this);
                }
            }
        } catch (IllegalAccessException e) {
            log.error("DynamicTp enhance origin executor of QueuedThreadPool failed.", e);
        }
    }

    @Override
    public void execute(Runnable runnable) {
        EnhancedRunnable enhanceTask = EnhancedRunnable.of(runnable, original);
        AwareManager.execute(original, enhanceTask);
        try {
            super.execute(enhanceTask);
        } catch (RejectedExecutionException e) {
            AwareManager.beforeReject(enhanceTask, original);
            throw e;
        }
    }
}
