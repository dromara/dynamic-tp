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

package org.dromara.dynamictp.test.adapter.webserver.tomcat;

import org.apache.tomcat.util.threads.TaskQueue;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.dromara.dynamictp.core.executor.NamedThreadFactory;
import org.dromara.dynamictp.starter.adapter.webserver.tomcat.TomcatExecutorProxy;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author kyao
 * @date 2023年09月15日 10:26
 */
public class TomcatExecutorProxyTest {

    @Test
    public void testParamAndStatus() throws InterruptedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 10, 5, TimeUnit.SECONDS, new TaskQueue(1), new NamedThreadFactory("测试线程池"));
        TomcatExecutorProxy proxy = new TomcatExecutorProxy(executor);

        Assert.assertEquals(executor.getCorePoolSize(), proxy.getCorePoolSize());
        Assert.assertEquals(executor.getMaximumPoolSize(), proxy.getMaximumPoolSize());
        Assert.assertEquals(executor.getCompletedTaskCount(), proxy.getCompletedTaskCount());
        Assert.assertEquals(executor.getLargestPoolSize(), proxy.getLargestPoolSize());
        Assert.assertEquals(executor.getThreadFactory(), proxy.getThreadFactory());
        Assert.assertEquals(executor.getKeepAliveTime(TimeUnit.SECONDS), proxy.getKeepAliveTime(TimeUnit.SECONDS));
        Assert.assertEquals(executor.getQueue(), proxy.getQueue());
        Assert.assertEquals(executor.allowsCoreThreadTimeOut(), proxy.allowsCoreThreadTimeOut());

        executor.shutdown();
        Thread.sleep(3000);
        Assert.assertTrue(executor.isShutdown());
        Assert.assertTrue(executor.isTerminated());
        Assert.assertFalse(executor.isTerminating());
    }
}
