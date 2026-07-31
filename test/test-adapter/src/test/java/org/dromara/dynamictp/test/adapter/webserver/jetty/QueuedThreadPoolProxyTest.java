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

package org.dromara.dynamictp.test.adapter.webserver.jetty;

import lombok.val;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.starter.adapter.webserver.jetty.QueuedThreadPoolProxy;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;

/**
 * @author kao
 */
public class QueuedThreadPoolProxyTest {

    @Test
    public void testParamAndStatus() {
        QueuedThreadPool executor = new QueuedThreadPool();
        BlockingQueue<Runnable> queue = (BlockingQueue<Runnable>) ReflectionUtil.getFieldValue("_jobs", executor);
        val threadGroup = (ThreadGroup) ReflectionUtil.getFieldValue("_threadGroup", executor);
        val threadFactory = (ThreadFactory) ReflectionUtil.getFieldValue("_threadFactory", executor);
        QueuedThreadPoolProxy proxy = new QueuedThreadPoolProxy(executor, queue, threadGroup, threadFactory);

        Assertions.assertEquals(executor.getMaxThreads(), proxy.getMaxThreads());
        Assertions.assertEquals(executor.getIdleTimeout(), proxy.getIdleTimeout());
        Assertions.assertEquals(executor.getMinThreads(), proxy.getMinThreads());
        Assertions.assertEquals(executor.getBusyThreads(), proxy.getBusyThreads());
        Assertions.assertEquals(executor.getReservedThreads(), proxy.getReservedThreads());
        Assertions.assertEquals(executor.getAvailableReservedThreads(), proxy.getAvailableReservedThreads());
        Assertions.assertEquals(executor.getIdleThreads(), proxy.getIdleThreads());
        Assertions.assertEquals(executor.getLeasedThreads(), proxy.getLeasedThreads());
        Assertions.assertEquals(executor.getLowThreadsThreshold(), proxy.getLowThreadsThreshold());
        Assertions.assertEquals(executor.getMaxAvailableThreads(), proxy.getMaxAvailableThreads());
        Assertions.assertEquals(executor.getMaxLeasedThreads(), proxy.getMaxLeasedThreads());
        Assertions.assertEquals(executor.getQueueSize(), proxy.getQueueSize());
        Assertions.assertEquals(executor.getReadyThreads(), proxy.getReadyThreads());
        Assertions.assertEquals(executor.getThreads(), proxy.getThreads());
        Assertions.assertEquals(executor.getThreadsPriority(), proxy.getThreadsPriority());
        Assertions.assertEquals(executor.getUtilizationRate(), proxy.getUtilizationRate(), 0.0);
        Assertions.assertEquals(executor.getStopTimeout(), proxy.getStopTimeout());
        Assertions.assertEquals(executor.getUtilizedThreads(), proxy.getUtilizedThreads());
    }
}
