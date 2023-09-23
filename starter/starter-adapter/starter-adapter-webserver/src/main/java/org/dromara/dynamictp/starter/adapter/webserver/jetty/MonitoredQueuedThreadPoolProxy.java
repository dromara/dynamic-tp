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
import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.support.task.runnable.EnhancedRunnable;
import org.eclipse.jetty.util.thread.MonitoredQueuedThreadPool;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;

/**
 * @author hanli
 * @since 1.1.4
 */
@Slf4j
public class MonitoredQueuedThreadPoolProxy extends MonitoredQueuedThreadPool {

    public MonitoredQueuedThreadPoolProxy(QueuedThreadPool threadPool, BlockingQueue<Runnable> queue) {
        super(threadPool.getMaxThreads(), threadPool.getMinThreads(), threadPool.getIdleTimeout(), queue);
    }

    @Override
    public void execute(Runnable runnable) {
        EnhancedRunnable enhanceTask = EnhancedRunnable.of(runnable, this);
        AwareManager.execute(this, enhanceTask);
        try {
            super.execute(enhanceTask);
        } catch (RejectedExecutionException e) {
            AwareManager.beforeReject(enhanceTask, this);
            throw e;
        }
    }
}
