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

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.entity.TpExecutorProps;
import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.executor.NamedThreadFactory;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.proxy.ThreadPoolExecutorProxy;
import org.dromara.dynamictp.spring.annotation.EnableDynamicTp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.*;

/**
 * @author hanli
 * @date 2023年09月15日 09:48
 */
@Slf4j
@EnableDynamicTp
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ThreadPoolExecutorProxyTest.class)
public class ThreadPoolExecutorProxyTest {

    @Test
    public void testParamAndStatus() throws InterruptedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 10, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("测试线程池"));
        ThreadPoolExecutorProxy proxy = new ThreadPoolExecutorProxy(executor);

        Assertions.assertEquals(executor.getCorePoolSize(), proxy.getCorePoolSize());
        Assertions.assertEquals(executor.getMaximumPoolSize(), proxy.getMaximumPoolSize());
        Assertions.assertEquals(executor.getActiveCount(), proxy.getActiveCount());
        Assertions.assertEquals(executor.getPoolSize(), proxy.getPoolSize());
        Assertions.assertEquals(executor.getCompletedTaskCount(), proxy.getCompletedTaskCount());
        Assertions.assertEquals(executor.getLargestPoolSize(), proxy.getLargestPoolSize());
        Assertions.assertEquals(executor.getTaskCount(), proxy.getTaskCount());
        Assertions.assertEquals(executor.getThreadFactory(), proxy.getThreadFactory());
        Assertions.assertEquals(executor.getKeepAliveTime(TimeUnit.SECONDS), proxy.getKeepAliveTime(TimeUnit.SECONDS));
        Assertions.assertEquals(executor.getQueue(), proxy.getQueue());
        Assertions.assertEquals(executor.allowsCoreThreadTimeOut(), proxy.allowsCoreThreadTimeOut());

        executor.shutdown();
        Thread.sleep(3000);
        Assertions.assertTrue(executor.isShutdown());
        Assertions.assertTrue(executor.isTerminated());
        Assertions.assertFalse(executor.isTerminating());
    }

    @Test
    public void runTimeoutTest() throws InterruptedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 10, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("测试线程池"));
        ThreadPoolExecutorProxy proxy = new ThreadPoolExecutorProxy(executor);
        ExecutorWrapper wrapper = new ExecutorWrapper("testExecutor", proxy);
        AwareManager.refresh(wrapper, buildProps());
        CountDownLatch latch = new CountDownLatch(1);
        proxy.execute(() -> {
            try {
                Thread.sleep(1000);
                latch.countDown();
            } catch (InterruptedException e) {
                // ignore
            }
        });
        latch.await();
    }

    @Test
    public void queueTimeoutTest() throws InterruptedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("测试线程池"));
        ThreadPoolExecutorProxy proxy = new ThreadPoolExecutorProxy(executor);
        ExecutorWrapper wrapper = new ExecutorWrapper("testExecutor", proxy);
        AwareManager.refresh(wrapper, buildProps());
        CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            try {
                proxy.execute(() -> {
                    try {
                        Thread.sleep(1000);
                        latch.countDown();
                    } catch (InterruptedException e) {
                        // ignore
                    }
                });
            } catch (RejectedExecutionException e) {
                latch.countDown();
                // ignore
            }
        }
        latch.await();
    }

    @Test
    public void rejectTest() throws InterruptedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1), new NamedThreadFactory("测试线程池"));
        ThreadPoolExecutorProxy proxy = new ThreadPoolExecutorProxy(executor);
        ExecutorWrapper wrapper = new ExecutorWrapper("testExecutor", proxy);
        AwareManager.refresh(wrapper, buildProps());
        CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            try {
                proxy.execute(() -> {
                    try {
                        Thread.sleep(1000);
                        latch.countDown();
                    } catch (InterruptedException e) {
                        // ignore
                    }
                });
            } catch (RejectedExecutionException e) {
                latch.countDown();
                // ignore
            }
        }
        latch.await();
    }

    private TpExecutorProps buildProps() {
        TpExecutorProps props = new TpExecutorProps();
        props.setRunTimeout(10);
        props.setQueueTimeout(10);
        return props;
    }
}
