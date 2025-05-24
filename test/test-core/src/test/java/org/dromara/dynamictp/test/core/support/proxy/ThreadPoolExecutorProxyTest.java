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

package org.dromara.dynamictp.test.core.support.proxy;

import org.dromara.dynamictp.core.executor.NamedThreadFactory;
import org.dromara.dynamictp.core.support.proxy.ThreadPoolExecutorProxy;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author devin
 */
public class ThreadPoolExecutorProxyTest {

    @Test
    public void testParametersAndStatus() throws InterruptedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                5, 10, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new NamedThreadFactory("ThriftTestPool"));
        
        ThreadPoolExecutorProxy proxy = new ThreadPoolExecutorProxy(executor);

        Assert.assertEquals(executor.getCorePoolSize(), proxy.getCorePoolSize());
        Assert.assertEquals(executor.getMaximumPoolSize(), proxy.getMaximumPoolSize());
        Assert.assertEquals(executor.getCompletedTaskCount(), proxy.getCompletedTaskCount());
        Assert.assertEquals(executor.getLargestPoolSize(), proxy.getLargestPoolSize());
        Assert.assertEquals(executor.getThreadFactory(), proxy.getThreadFactory());
        Assert.assertEquals(executor.getKeepAliveTime(TimeUnit.SECONDS), proxy.getKeepAliveTime(TimeUnit.SECONDS));
        Assert.assertEquals(executor.getQueue(), proxy.getQueue());
        Assert.assertEquals(executor.allowsCoreThreadTimeOut(), proxy.allowsCoreThreadTimeOut());

        proxy.setCorePoolSize(6);
        Assert.assertEquals(6, proxy.getCorePoolSize());
        
        proxy.setMaximumPoolSize(12);
        Assert.assertEquals(12, proxy.getMaximumPoolSize());
        
        proxy.setKeepAliveTime(120, TimeUnit.SECONDS);
        Assert.assertEquals(120, proxy.getKeepAliveTime(TimeUnit.SECONDS));
        
        executor.shutdown();
        Thread.sleep(1000);
        Assert.assertTrue(executor.isShutdown());
    }
}
