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

package org.dromara.dynamictp.test.core.thread.proxy;

import org.dromara.dynamictp.core.executor.NamedThreadFactory;
import org.dromara.dynamictp.core.support.proxy.ThreadPoolExecutorProxy;
import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author hanli
 * @date 2023年09月15日 09:48
 */
class ThreadPoolExecutorTest {

    @Test
    void testParamAndStatus() throws InterruptedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 10, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("测试线程池"));
        ThreadPoolExecutorProxy proxy = new ThreadPoolExecutorProxy(executor);

        assertEquals(executor.getCorePoolSize(), proxy.getCorePoolSize());
        assertEquals(executor.getMaximumPoolSize(), proxy.getMaximumPoolSize());
        assertEquals(executor.getActiveCount(), proxy.getActiveCount());
        assertEquals(executor.getPoolSize(), proxy.getPoolSize());
        assertEquals(executor.getCompletedTaskCount(), proxy.getCompletedTaskCount());
        assertEquals(executor.getLargestPoolSize(), proxy.getLargestPoolSize());
        assertEquals(executor.getTaskCount(), proxy.getTaskCount());
        assertEquals(executor.getThreadFactory(), proxy.getThreadFactory());
        assertEquals(executor.getKeepAliveTime(TimeUnit.SECONDS), proxy.getKeepAliveTime(TimeUnit.SECONDS));
        assertEquals(executor.getQueue(), proxy.getQueue());
        assertEquals(executor.allowsCoreThreadTimeOut(), proxy.allowsCoreThreadTimeOut());

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        assertTrue(executor.isShutdown());
        assertTrue(executor.isTerminated());
        assertFalse(executor.isTerminating());
    }
}
