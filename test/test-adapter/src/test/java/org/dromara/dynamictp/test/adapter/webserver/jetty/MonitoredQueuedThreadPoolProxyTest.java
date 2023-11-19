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

import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.starter.adapter.webserver.jetty.MonitoredQueuedThreadPoolProxy;
import org.eclipse.jetty.util.thread.MonitoredQueuedThreadPool;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;

/**
 * @author kao
 * @date 2023年09月15日 11:50
 */
public class MonitoredQueuedThreadPoolProxyTest {

    @Test
    public void testParam() {
        MonitoredQueuedThreadPool executor = new MonitoredQueuedThreadPool();
        BlockingQueue<Runnable> queue = (BlockingQueue<Runnable>) ReflectionUtil.getFieldValue("_jobs", executor);
        MonitoredQueuedThreadPoolProxy proxy = new MonitoredQueuedThreadPoolProxy(executor, queue);

        Assert.assertEquals(executor.getMaxBusyThreads(), proxy.getMaxBusyThreads());
        Assert.assertEquals(executor.getTasks(), proxy.getTasks());
        Assert.assertEquals(executor.getAverageQueueLatency(), proxy.getAverageQueueLatency());
        Assert.assertEquals(executor.getMaxQueueLatency(), proxy.getMaxQueueLatency());
        Assert.assertEquals(executor.getMaxThreads(), proxy.getMaxThreads());
        Assert.assertEquals(executor.getIdleTimeout(), proxy.getIdleTimeout());
        Assert.assertEquals(executor.getMaxQueueSize(), proxy.getMaxQueueSize());
        Assert.assertEquals(executor.getMinThreads(), proxy.getMinThreads());
        Assert.assertEquals(executor.getBusyThreads(), proxy.getBusyThreads());
        Assert.assertEquals(executor.getReservedThreads(), proxy.getReservedThreads());
        Assert.assertEquals(executor.getAvailableReservedThreads(), proxy.getAvailableReservedThreads());
        Assert.assertEquals(executor.getIdleThreads(), proxy.getIdleThreads());
        Assert.assertEquals(executor.getLeasedThreads(), proxy.getLeasedThreads());
        Assert.assertEquals(executor.getLowThreadsThreshold(), proxy.getLowThreadsThreshold());
        Assert.assertEquals(executor.getMaxAvailableThreads(), proxy.getMaxAvailableThreads());
        Assert.assertEquals(executor.getMaxLeasedThreads(), proxy.getMaxLeasedThreads());
        Assert.assertEquals(executor.getQueueSize(), proxy.getQueueSize());
        Assert.assertEquals(executor.getReadyThreads(), proxy.getReadyThreads());
        Assert.assertEquals(executor.getThreads(), proxy.getThreads());
        Assert.assertEquals(executor.getThreadsPriority(), proxy.getThreadsPriority());
        Assert.assertEquals(executor.getUtilizationRate(), proxy.getUtilizationRate(), 0.0);
        Assert.assertEquals(executor.getStopTimeout(), proxy.getStopTimeout());
        Assert.assertEquals(executor.getUtilizedThreads(), proxy.getUtilizedThreads());
    }
}
