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

package org.dromara.dynamictp.test.core.support;

import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.aware.PerformanceMonitorAware;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolBuilder;
import org.dromara.dynamictp.core.support.ThreadPoolStatProvider;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.spring.annotation.EnableDynamicTp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertTrue;

/**
 * shutdownNow 丢弃的队列任务不会经过 afterExecute/afterReject，
 * 其在 stopWatchMap 中的 DtpRunnable 引用应被显式清理，否则长期运行会内存泄漏（issue #584）。
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ThreadPoolStatProviderLeakTest.TestConfig.class)
public class ThreadPoolStatProviderLeakTest {

    @org.springframework.context.annotation.Configuration
    @EnableDynamicTp
    static class TestConfig {
    }


    private Map<?, ?> stopWatchMapOf(DtpExecutor executor) throws Exception {
        // 取 AwareManager 静态列表中的 PerformanceMonitorAware 单例
        Field listField = Class.forName("org.dromara.dynamictp.core.aware.AwareManager")
                .getDeclaredField("EXECUTOR_AWARE_LIST");
        listField.setAccessible(true);
        Collection<?> awares = (Collection<?>) listField.get(null);
        Object aware = awares.stream()
                .filter(a -> a instanceof PerformanceMonitorAware)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("PerformanceMonitorAware not registered"));

        Field providersField = Class.forName("org.dromara.dynamictp.core.aware.TaskStatAware")
                .getDeclaredField("statProviders");
        providersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<?, ThreadPoolStatProvider> providers =
                (Map<?, ThreadPoolStatProvider>) providersField.get(aware);
        ThreadPoolStatProvider provider = providers.get(executor);
        if (provider == null) {
            return null;
        }
        Field swField = ThreadPoolStatProvider.class.getDeclaredField("stopWatchMap");
        swField.setAccessible(true);
        return (Map<?, ?>) swField.get(provider);
    }

    @Test
    public void shutdownNow_shouldClearStopWatchEntriesOfDiscardedTasks() throws Exception {
        DtpExecutor executor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("leakTestDtpExecutor")
                .corePoolSize(1)
                .maximumPoolSize(1)
                .queueCapacity(50)
                .buildDynamic();
        ExecutorWrapper wrapper = ExecutorWrapper.of(executor);
        wrapper.initialize();
        DtpRegistry.registerExecutor(wrapper, "test");

        // 占住唯一的工作线程，使后续提交的任务滞留在队列中（提交时即写入 stopWatchMap）
        CountDownLatch blocker = new CountDownLatch(1);
        executor.execute(() -> {
            try {
                blocker.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        for (int i = 0; i < 5; i++) {
            executor.execute(() -> { });
        }

        Map<?, ?> stopWatchMap = stopWatchMapOf(executor);
        assertTrue("queued tasks should be tracked in stopWatchMap before shutdownNow",
                stopWatchMap.size() >= 5);

        // shutdownNow 返回后，被丢弃的队列任务应立即被清理；
        // 最多只剩正在运行的 blocker 任务（其 afterExecute 尚未执行）
        executor.shutdownNow();
        assertTrue("discarded queued tasks must not stay in stopWatchMap after shutdownNow, entries: "
                + stopWatchMap.size(), stopWatchMap.size() <= 1);

        blocker.countDown();

        // blocker 任务的 afterExecute 在 worker 线程上异步执行，轮询等待其完成清理
        long deadline = System.currentTimeMillis() + 5000;
        while (!stopWatchMap.isEmpty() && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        assertTrue("stopWatchMap should be empty after shutdownNow, leaked entries: " + stopWatchMap.size(),
                stopWatchMap.isEmpty());
    }
}
