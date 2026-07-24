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

package org.dromara.dynamictp.test.core;

import org.dromara.dynamictp.common.entity.DtpExecutorProps;
import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.converter.ExecutorConverter;
import org.dromara.dynamictp.core.executor.ExecutorType;
import org.dromara.dynamictp.core.executor.VirtualThreadExecutorFactory;
import org.dromara.dynamictp.core.support.DtpLifecycleSupport;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.VirtualThreadExecutorAdapter;
import org.dromara.dynamictp.core.support.proxy.VirtualThreadExecutorProxy;
import org.dromara.dynamictp.core.support.task.runnable.EnhancedRunnable;
import org.dromara.dynamictp.spring.DtpPostProcessor;
import org.dromara.dynamictp.spring.holder.SpringContextHolder;
import org.dromara.dynamictp.spring.support.SimpleAsyncTaskExecutorAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.mock.env.MockEnvironment;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end style coverage for virtual-thread executor support.
 *
 * @author yanhom
 * @since 1.3.0
 */
@Execution(ExecutionMode.SAME_THREAD)
class VirtualThreadExecutorIntegrationTest {

    private static final String POOL_NAME = "vt-integration-pool";

    private GenericApplicationContext context;

    private ApplicationContext originalContext;

    @BeforeEach
    void setUp() throws Exception {
        originalContext = springContext();
        context = new GenericApplicationContext();
        context.refresh();
        new SpringContextHolder().setApplicationContext(context);
        new DtpRegistry(DtpProperties.getInstance());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (DtpRegistry.getAllExecutorNames().contains(POOL_NAME)) {
            DtpRegistry.unregisterExecutor(POOL_NAME);
        }
        DtpProperties.getInstance().setExecutors(null);
        context.close();
        setSpringContext(originalContext);
    }

    @Test
    void factoryCreatesRealVirtualThreadPerTaskExecutor() throws Exception {
        assumeJdk21Plus();

        ExecutorService executor = VirtualThreadExecutorFactory.newThreadPerTaskExecutor("vt-factory-");
        AtomicBoolean isVirtual = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        try {
            executor.execute(() -> {
                isVirtual.set(Thread.currentThread().isVirtual());
                latch.countDown();
            });
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertTrue(isVirtual.get(), "task should run on a JDK virtual thread");
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void executorTypeMapsVirtualNameToProxyClass() {
        assertSame(VirtualThreadExecutorProxy.class, ExecutorType.getClass("virtual"));
        assertSame(VirtualThreadExecutorProxy.class, ExecutorType.VIRTUAL.getClazz());
    }

    @Test
    void adapterReportsUnsupportedPoolMetricsAsMinusOne() {
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(
                java.util.concurrent.Executors.newSingleThreadExecutor());
        VirtualThreadExecutorAdapter adapter = new VirtualThreadExecutorAdapter(proxy);
        try {
            assertEquals(-1, adapter.getCorePoolSize());
            assertEquals(-1, adapter.getMaximumPoolSize());
            assertEquals(-1, adapter.getPoolSize());
            assertEquals(-1, adapter.getActiveCount());
            assertEquals(-1, adapter.getLargestPoolSize());
            assertEquals(-1, adapter.getTaskCount());
            assertEquals(-1, adapter.getCompletedTaskCount());
            assertEquals(0, adapter.getQueueSize());
            assertEquals(0, adapter.getQueueRemainingCapacity());
            assertTrue(adapter.getQueueType().contains("Unsupported"));
            assertSame(proxy, adapter.getOriginal());
        } finally {
            proxy.shutdownNow();
        }
    }

    @Test
    void metricsConversionDoesNotThrowForVirtualExecutor() {
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(
                java.util.concurrent.Executors.newSingleThreadExecutor());
        ExecutorWrapper wrapper = new ExecutorWrapper(POOL_NAME, proxy);
        DtpRegistry.registerExecutor(wrapper, "test");
        try {
            ThreadPoolStats stats = ExecutorConverter.toMetrics(wrapper);
            assertNotNull(stats);
            assertEquals(POOL_NAME, stats.getPoolName());
            assertEquals(-1, stats.getCorePoolSize());
            assertEquals(-1, stats.getMaximumPoolSize());
            assertEquals(-1, stats.getActiveCount());
            assertEquals(-1, stats.getPoolSize());
            assertFalse(stats.isDynamic());
        } finally {
            proxy.shutdownNow();
        }
    }

    @Test
    void proxyExecuteSubmitAndInvokeAllEnhanceTasks() throws Exception {
        assumeJdk21Plus();

        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(
                VirtualThreadExecutorFactory.newThreadPerTaskExecutor("vt-proxy-"));
        AtomicInteger wrapCount = new AtomicInteger();
        AtomicReference<Boolean> ranOnVirtual = new AtomicReference<>();
        proxy.setTaskWrappers(Collections.singletonList(r -> {
            wrapCount.incrementAndGet();
            return () -> {
                ranOnVirtual.set(Thread.currentThread().isVirtual());
                r.run();
            };
        }));

        ExecutorWrapper wrapper = new ExecutorWrapper(POOL_NAME, proxy);
        DtpRegistry.registerExecutor(wrapper, "test");
        AwareManager.register(wrapper);

        try {
            CountDownLatch latch = new CountDownLatch(1);
            proxy.execute(latch::countDown);
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertTrue(Boolean.TRUE.equals(ranOnVirtual.get()));

            Future<Integer> future = proxy.submit(() -> 42);
            assertEquals(42, future.get(5, TimeUnit.SECONDS));

            List<Future<Integer>> futures = proxy.invokeAll(List.of(() -> 1, () -> 2));
            assertEquals(3, futures.get(0).get() + futures.get(1).get());

            // execute + submit + 2 invokeAll tasks
            assertTrue(wrapCount.get() >= 4);

            // performance keys cleared after completion
            Map<?, ?> stopWatchMap = getStopWatchMap(wrapper);
            assertTrue(stopWatchMap.isEmpty(), "stopWatchMap should be empty after tasks complete");
        } finally {
            proxy.shutdownNow();
        }
    }

    @Test
    void concurrentExecuteDoesNotLoseTasks() throws Exception {
        assumeJdk21Plus();

        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(
                VirtualThreadExecutorFactory.newThreadPerTaskExecutor("vt-concurrent-"));
        int taskCount = 200;
        CountDownLatch latch = new CountDownLatch(taskCount);
        AtomicInteger completed = new AtomicInteger();
        try {
            for (int i = 0; i < taskCount; i++) {
                proxy.execute(() -> {
                    completed.incrementAndGet();
                    latch.countDown();
                });
            }
            assertTrue(latch.await(15, TimeUnit.SECONDS));
            assertEquals(taskCount, completed.get());
        } finally {
            proxy.shutdownNow();
        }
    }

    @Test
    void lifecycleDestroyShutsDownVirtualProxy() {
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(
                java.util.concurrent.Executors.newSingleThreadExecutor());
        ExecutorWrapper wrapper = new ExecutorWrapper(POOL_NAME, proxy);
        DtpRegistry.registerExecutor(wrapper, "test");

        assertFalse(proxy.isShutdown());
        DtpLifecycleSupport.destroy(wrapper);
        assertTrue(proxy.isShutdown());
    }

    @Test
    void refreshUpdatesAliasTaskWrappersAndTimeoutsWithInvalidCoreParams() throws Exception {
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(
                java.util.concurrent.Executors.newSingleThreadExecutor());
        proxy.setThreadPoolAliasName("old-alias");
        proxy.setRunTimeout(100);
        proxy.setQueueTimeout(200);

        DtpPostProcessor processor = new DtpPostProcessor();
        processor.setBeanFactory(new DefaultListableBeanFactory());
        processor.setEnvironment(new MockEnvironment());
        processor.postProcessAfterInitialization(proxy, POOL_NAME);

        ExecutorWrapper wrapper = DtpRegistry.getExecutorWrapper(POOL_NAME);
        AwareManager.register(wrapper);

        AtomicBoolean wrapped = new AtomicBoolean();
        // register a named wrapper via TaskWrappers is heavy; just set directly then refresh overrides
        wrapper.setTaskWrappers(Collections.singletonList(r -> {
            wrapped.set(true);
            return r;
        }));

        DtpExecutorProps props = new DtpExecutorProps();
        props.setThreadPoolName(POOL_NAME);
        props.setThreadPoolAliasName("new-alias");
        props.setCorePoolSize(-1);
        props.setMaximumPoolSize(0);
        props.setKeepAliveTime(-1);
        props.setRunTimeout(1500);
        props.setQueueTimeout(2500);
        props.setTryInterrupt(true);
        props.setNotifyEnabled(false);
        props.setRejectEnhanced(false);
        props.setWaitForTasksToCompleteOnShutdown(false);
        props.setAwaitTerminationSeconds(7);
        props.setRejectedHandlerType("AbortPolicy");

        DtpRegistry.refresh(props);

        ExecutorWrapper after = DtpRegistry.getExecutorWrapper(POOL_NAME);
        assertEquals("new-alias", after.getThreadPoolAliasName());
        assertEquals(1500, after.getThreadPoolStatProvider().getRunTimeout());
        assertEquals(2500, after.getThreadPoolStatProvider().getQueueTimeout());
        assertTrue(after.getThreadPoolStatProvider().isTryInterrupt());
        assertFalse(after.isNotifyEnabled());
        assertFalse(after.isRejectEnhanced());
        assertFalse(after.isWaitForTasksToCompleteOnShutdown());
        assertEquals(7, after.getAwaitTerminationSeconds());

        CountDownLatch latch = new CountDownLatch(1);
        after.getExecutor().execute(latch::countDown);
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        proxy.shutdownNow();
    }

    @Test
    void simpleAsyncAdapterLifecycleAndExecute() throws Exception {
        SimpleAsyncTaskExecutor simple = new SimpleAsyncTaskExecutor("simple-vt-");
        SimpleAsyncTaskExecutorAdapter adapter = new SimpleAsyncTaskExecutorAdapter(simple);
        CountDownLatch latch = new CountDownLatch(1);
        adapter.execute(latch::countDown);
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertFalse(adapter.isShutdown());

        adapter.shutdown();
        assertTrue(adapter.isShutdown());
        assertTrue(adapter.isTerminated());
        assertTrue(adapter.awaitTermination(1, TimeUnit.SECONDS));
        assertThrows(IllegalStateException.class, () -> adapter.execute(() -> {
        }));
    }

    @Test
    void simpleAsyncRegisteredThroughPostProcessorEnhancesAndIsIdempotent() throws Exception {
        DtpPostProcessor processor = new DtpPostProcessor();
        processor.setBeanFactory(new DefaultListableBeanFactory());
        processor.setEnvironment(new MockEnvironment()
                .withProperty("spring.threads.virtual.enabled", "true"));

        SimpleAsyncTaskExecutor simple = new SimpleAsyncTaskExecutor("app-vt-");
        Object returned = processor.postProcessAfterInitialization(simple, POOL_NAME);
        // original bean is preserved for Spring Boot virtual thread executor
        assertSame(simple, returned);

        ExecutorWrapper wrapper = DtpRegistry.getExecutorWrapper(POOL_NAME);
        assertTrue(wrapper.isVirtualThreadExecutor());

        AtomicInteger wrapCount = new AtomicInteger();
        wrapper.setTaskWrappers(Collections.singletonList(r -> {
            wrapCount.incrementAndGet();
            return r;
        }));

        CountDownLatch latch = new CountDownLatch(2);
        // path 1: Spring bean
        simple.execute(latch::countDown);
        // path 2: registry adapter (would double-decorate without idempotent decorate)
        wrapper.getExecutor().execute(latch::countDown);

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(2, wrapCount.get());

        VirtualThreadExecutorProxy proxy =
                (VirtualThreadExecutorProxy) wrapper.getExecutor().getOriginal();
        Runnable once = proxy.decorate(() -> {
        });
        assertInstanceOf(EnhancedRunnable.class, once);
        assertSame(once, proxy.decorate(once));
    }

    @Test
    void rejectHandlerTypeCanBeUpdatedOnVirtualAdapter() {
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(
                java.util.concurrent.Executors.newSingleThreadExecutor());
        ExecutorWrapper wrapper = new ExecutorWrapper(POOL_NAME, proxy);
        DtpRegistry.registerExecutor(wrapper, "test");
        try {
            assertEquals("unknown", wrapper.getExecutor().getRejectHandlerType());
            // rejectEnhanced=true (default) wraps with RejectHandlerGetter proxy;
            // type must stay as original simple name, not $ProxyN.
            wrapper.setRejectHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
            assertEquals("CallerRunsPolicy", wrapper.getExecutor().getRejectHandlerType());

            wrapper.setRejectEnhanced(false);
            wrapper.setRejectHandler(new java.util.concurrent.ThreadPoolExecutor.DiscardPolicy());
            assertEquals("DiscardPolicy", wrapper.getExecutor().getRejectHandlerType());

            // still never rejects
            CountDownLatch latch = new CountDownLatch(1);
            wrapper.getExecutor().execute(latch::countDown);
            assertTrue(latch.await(3, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        } finally {
            proxy.shutdownNow();
        }
    }

    @Test
    void captureAndMainFieldsWorkForVirtualExecutor() {
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(
                java.util.concurrent.Executors.newSingleThreadExecutor());
        ExecutorWrapper wrapper = new ExecutorWrapper(POOL_NAME, proxy);
        try {
            var main = ExecutorConverter.toMainFields(wrapper);
            assertEquals(POOL_NAME, main.getThreadPoolName());
            assertEquals(-1, main.getCorePoolSize());
            assertEquals(-1, main.getMaxPoolSize());

            ExecutorWrapper captured = wrapper.capture();
            assertNotNull(captured.getExecutor());
            assertEquals(-1, captured.getExecutor().getActiveCount());
        } finally {
            proxy.shutdownNow();
        }
    }

    @Test
    void manyShortTasksProduceStablePerformanceSnapshot() throws Exception {
        assumeJdk21Plus();

        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(
                VirtualThreadExecutorFactory.newThreadPerTaskExecutor("vt-perf-"));
        ExecutorWrapper wrapper = new ExecutorWrapper(POOL_NAME, proxy);
        DtpRegistry.registerExecutor(wrapper, "test");
        AwareManager.register(wrapper);

        int n = 50;
        CountDownLatch latch = new CountDownLatch(n);
        try {
            for (int i = 0; i < n; i++) {
                proxy.execute(() -> {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    latch.countDown();
                });
            }
            assertTrue(latch.await(15, TimeUnit.SECONDS));

            // allow afterExecute to finish
            Thread.sleep(50);
            Map<?, ?> stopWatchMap = getStopWatchMap(wrapper);
            assertTrue(stopWatchMap.isEmpty(),
                    "leaked performance keys: " + stopWatchMap.size());

            ThreadPoolStats stats = ExecutorConverter.toMetrics(wrapper);
            assertNotNull(stats);
            // tps/rt may be 0 if snapshot window empty after reset, but conversion must succeed
            assertTrue(stats.getTps() >= 0);
        } finally {
            proxy.shutdownNow();
        }
    }

    private static void assumeJdk21Plus() {
        Assumptions.assumeTrue(Runtime.version().feature() >= 21,
                "requires JDK 21+ for real virtual threads");
    }

    private Map<?, ?> getStopWatchMap(ExecutorWrapper wrapper) throws Exception {
        Field field = wrapper.getThreadPoolStatProvider().getClass().getDeclaredField("stopWatchMap");
        field.setAccessible(true);
        return (Map<?, ?>) field.get(wrapper.getThreadPoolStatProvider());
    }

    private ApplicationContext springContext() throws Exception {
        Field field = SpringContextHolder.class.getDeclaredField("context");
        field.setAccessible(true);
        return (ApplicationContext) field.get(null);
    }

    private void setSpringContext(ApplicationContext applicationContext) throws Exception {
        Field field = SpringContextHolder.class.getDeclaredField("context");
        field.setAccessible(true);
        field.set(null, applicationContext);
    }
}
