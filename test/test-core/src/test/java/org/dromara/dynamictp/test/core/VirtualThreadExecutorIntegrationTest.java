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

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.dromara.dynamictp.common.entity.DtpExecutorProps;
import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.aware.ExecutorAware;
import org.dromara.dynamictp.core.converter.ExecutorConverter;
import org.dromara.dynamictp.core.executor.ExecutorType;
import org.dromara.dynamictp.core.executor.VirtualThreadExecutorFactory;
import org.dromara.dynamictp.core.monitor.collector.MicroMeterCollector;
import org.dromara.dynamictp.core.support.DtpLifecycleSupport;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.VirtualThreadExecutorAdapter;
import org.dromara.dynamictp.core.support.adapter.ExecutorAdapter;
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
import org.springframework.core.task.TaskRejectedException;
import org.springframework.mock.env.MockEnvironment;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

//    @Test
//    void factoryCreatesRealVirtualThreadPerTaskExecutor() throws Exception {
//        assumeJdk21Plus();
//
//        ExecutorService executor = VirtualThreadExecutorFactory.newThreadPerTaskExecutor("vt-factory-");
//        AtomicBoolean isVirtual = new AtomicBoolean(false);
//        CountDownLatch latch = new CountDownLatch(1);
//        try {
//            executor.execute(() -> {
//                isVirtual.set(Thread.currentThread().isVirtual());
//                latch.countDown();
//            });
//            assertTrue(latch.await(5, TimeUnit.SECONDS));
//            assertTrue(isVirtual.get(), "task should run on a JDK virtual thread");
//        } finally {
//            executor.shutdownNow();
//        }
//    }

    @Test
    void executorTypeMapsVirtualNameToProxyClass() {
        assertSame(VirtualThreadExecutorProxy.class, ExecutorType.getClass("virtual"));
        assertSame(VirtualThreadExecutorProxy.class, ExecutorType.VIRTUAL.getClazz());
    }

    @Test
    void adapterReportsSizingMetricsAsUnsupportedButTracksTasks() {
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(
                java.util.concurrent.Executors.newSingleThreadExecutor());
        VirtualThreadExecutorAdapter adapter = new VirtualThreadExecutorAdapter(proxy);
        try {
            // no fixed pool, no queue
            assertEquals(-1, adapter.getCorePoolSize());
            assertEquals(-1, adapter.getMaximumPoolSize());
            assertEquals(-1, adapter.getKeepAliveTime(TimeUnit.MILLISECONDS));
            assertEquals(0, adapter.getQueueSize());
            assertEquals(0, adapter.getQueueRemainingCapacity());
            assertEquals(VirtualThreadExecutorAdapter.QUEUE_TYPE, adapter.getQueueType());
            // task statistics are tracked, starting from zero
            assertEquals(0, adapter.getPoolSize());
            assertEquals(0, adapter.getActiveCount());
            assertEquals(0, adapter.getLargestPoolSize());
            assertEquals(0, adapter.getTaskCount());
            assertEquals(0, adapter.getCompletedTaskCount());
            assertSame(proxy, adapter.getOriginal());
        } finally {
            proxy.shutdownNow();
        }
    }

    @Test
    void taskCountersTrackSubmissionsAndCompletions() throws Exception {
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(
                java.util.concurrent.Executors.newSingleThreadExecutor());
        ExecutorWrapper wrapper = new ExecutorWrapper(POOL_NAME, proxy);
        DtpRegistry.registerExecutor(wrapper, "test");
        AwareManager.register(wrapper);
        try {
            int taskCount = 20;
            CountDownLatch latch = new CountDownLatch(taskCount);
            for (int i = 0; i < taskCount; i++) {
                proxy.execute(latch::countDown);
            }
            // a task that throws must still be counted as completed
            proxy.execute(() -> {
                throw new IllegalStateException("boom");
            });
            assertTrue(latch.await(10, TimeUnit.SECONDS));

            ExecutorAdapter<?> adapter = wrapper.getExecutor();
            int expected = taskCount + 1;
            // counters are updated when a task starts, so wait for the last one to finish
            waitUntil(() -> adapter.getCompletedTaskCount() == expected);
            assertEquals(expected, adapter.getTaskCount());
            assertEquals(expected, adapter.getCompletedTaskCount());
            assertEquals(0, adapter.getActiveCount());
            assertEquals(0, adapter.getPoolSize());
            assertTrue(adapter.getLargestPoolSize() >= 1,
                    "largest pool size should have been recorded, was " + adapter.getLargestPoolSize());
        } finally {
            proxy.shutdownNow();
        }
    }

    @Test
    void rejectedTaskIsNotCounted() {
        ExecutorService delegate = java.util.concurrent.Executors.newSingleThreadExecutor();
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(delegate);
        ExecutorWrapper wrapper = new ExecutorWrapper(POOL_NAME, proxy);
        DtpRegistry.registerExecutor(wrapper, "test");
        AwareManager.register(wrapper);

        delegate.shutdown();
        assertThrows(RejectedExecutionException.class, () -> proxy.execute(() -> { }));

        ExecutorAdapter<?> adapter = wrapper.getExecutor();
        assertEquals(0, adapter.getTaskCount(), "a rejected task must not be counted");
        assertEquals(0, adapter.getActiveCount(), "active count must not leak on reject");
        assertEquals(0, adapter.getCompletedTaskCount());
        assertEquals(0, adapter.getLargestPoolSize(),
                "a task that never ran must not raise the high-water mark");
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
            assertEquals(0, stats.getActiveCount());
            assertEquals(0, stats.getPoolSize());
            assertEquals(0, stats.getTaskCount());
            assertFalse(stats.isDynamic());
            assertTrue(stats.isVirtual());
        } finally {
            proxy.shutdownNow();
        }
    }

//    @Test
//    void proxyExecuteSubmitAndInvokeAllEnhanceTasks() throws Exception {
//        assumeJdk21Plus();
//
//        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(
//                VirtualThreadExecutorFactory.newThreadPerTaskExecutor("vt-proxy-"));
//        AtomicInteger wrapCount = new AtomicInteger();
//        AtomicReference<Boolean> ranOnVirtual = new AtomicReference<>();
//        proxy.setTaskWrappers(Collections.singletonList(r -> {
//            wrapCount.incrementAndGet();
//            return () -> {
//                ranOnVirtual.set(Thread.currentThread().isVirtual());
//                r.run();
//            };
//        }));
//
//        ExecutorWrapper wrapper = new ExecutorWrapper(POOL_NAME, proxy);
//        DtpRegistry.registerExecutor(wrapper, "test");
//        AwareManager.register(wrapper);
//
//        try {
//            CountDownLatch latch = new CountDownLatch(1);
//            proxy.execute(latch::countDown);
//            assertTrue(latch.await(5, TimeUnit.SECONDS));
//            assertTrue(Boolean.TRUE.equals(ranOnVirtual.get()));
//
//            Future<Integer> future = proxy.submit(() -> 42);
//            assertEquals(42, future.get(5, TimeUnit.SECONDS));
//
//            List<Future<Integer>> futures = proxy.invokeAll(List.of(() -> 1, () -> 2));
//            assertEquals(3, futures.get(0).get() + futures.get(1).get());
//
//            // execute + submit + 2 invokeAll tasks
//            assertTrue(wrapCount.get() >= 4);
//
//            // performance keys cleared after completion
//            Map<?, ?> stopWatchMap = getStopWatchMap(wrapper);
//            assertTrue(stopWatchMap.isEmpty(), "stopWatchMap should be empty after tasks complete");
//        } finally {
//            proxy.shutdownNow();
//        }
//    }

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

            ExecutorWrapper wrapper = new ExecutorWrapper(POOL_NAME, proxy);
            assertEquals(taskCount, wrapper.getExecutor().getTaskCount());
            waitUntil(() -> wrapper.getExecutor().getCompletedTaskCount() == taskCount);
            assertEquals(taskCount, wrapper.getExecutor().getCompletedTaskCount());
            assertTrue(wrapper.getExecutor().getLargestPoolSize() > 1,
                    "virtual threads run concurrently, largest pool size was "
                            + wrapper.getExecutor().getLargestPoolSize());
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
        // ExecutorService contract: rejection is signalled with RejectedExecutionException
        assertThrows(RejectedExecutionException.class, () -> adapter.execute(() -> {
        }));

        // the adapted executor belongs to the Spring container, dtp must not close it
        CountDownLatch stillUsable = new CountDownLatch(1);
        simple.execute(stillUsable::countDown);
        assertTrue(stillUsable.await(5, TimeUnit.SECONDS),
                "the Spring owned SimpleAsyncTaskExecutor must stay usable after dtp shutdown");
        simple.close();
    }

    @Test
    void simpleAsyncWithVirtualThreadsIsRegisteredWithoutGlobalProperty() {
        assumeJdk21Plus();

        SimpleAsyncTaskExecutor simple = new SimpleAsyncTaskExecutor("manual-vt-");
        simple.setVirtualThreads(true);

        DtpPostProcessor processor = new DtpPostProcessor();
        processor.setBeanFactory(new DefaultListableBeanFactory());
        // property intentionally left off: the instance itself opted into virtual threads
        processor.setEnvironment(new MockEnvironment());

        Object returned = processor.postProcessAfterInitialization(simple, POOL_NAME);
        assertSame(simple, returned);
        assertTrue(DtpRegistry.getExecutorWrapper(POOL_NAME).isVirtualThreadExecutor());
        simple.close();
    }

    @Test
    void simpleAsyncUserTaskDecoratorIsPreserved() throws Exception {
        assumeJdk21Plus();

        SimpleAsyncTaskExecutor simple = new SimpleAsyncTaskExecutor("decorated-vt-");
        simple.setVirtualThreads(true);
        AtomicInteger userDecoratorCalls = new AtomicInteger();
        simple.setTaskDecorator(r -> {
            userDecoratorCalls.incrementAndGet();
            return r;
        });

        DtpPostProcessor processor = new DtpPostProcessor();
        processor.setBeanFactory(new DefaultListableBeanFactory());
        processor.setEnvironment(new MockEnvironment()
                .withProperty("spring.threads.virtual.enabled", "true"));
        processor.postProcessAfterInitialization(simple, POOL_NAME);

        CountDownLatch latch = new CountDownLatch(1);
        simple.execute(latch::countDown);
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(1, userDecoratorCalls.get(), "the user's TaskDecorator must still be applied");
        simple.close();
    }

    @Test
    void simpleAsyncRegisteredThroughPostProcessorEnhancesAndIsIdempotent() throws Exception {
        assumeJdk21Plus();

        DtpPostProcessor processor = new DtpPostProcessor();
        processor.setBeanFactory(new DefaultListableBeanFactory());
        processor.setEnvironment(new MockEnvironment()
                .withProperty("spring.threads.virtual.enabled", "true"));

        SimpleAsyncTaskExecutor simple = new SimpleAsyncTaskExecutor("app-vt-");
        simple.setVirtualThreads(true);
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

        // both entry points are counted exactly once
        waitUntil(() -> wrapper.getExecutor().getCompletedTaskCount() == 2);
        assertEquals(2, wrapper.getExecutor().getTaskCount());
        assertEquals(2, wrapper.getExecutor().getCompletedTaskCount());

        VirtualThreadExecutorProxy proxy =
                (VirtualThreadExecutorProxy) wrapper.getExecutor().getOriginal();
        Runnable once = proxy.decorate(() -> {
        });
        assertInstanceOf(EnhancedRunnable.class, once);
        assertSame(once, proxy.decorate(once));
    }

    @Test
    void decorateHasNoSideEffectUntilTaskStarts() throws Exception {
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(
                java.util.concurrent.Executors.newSingleThreadExecutor());
        ExecutorWrapper wrapper = new ExecutorWrapper(POOL_NAME, proxy);
        DtpRegistry.registerExecutor(wrapper, "test");
        AwareManager.register(wrapper);
        try {
            Runnable decorated = proxy.decorate(() -> { });
            // decorate is also used as a Spring TaskDecorator: a task that never runs
            // (because Spring rejects it after decorating) must leave nothing behind
            assertEquals(0, wrapper.getExecutor().getTaskCount());
            assertEquals(0, wrapper.getExecutor().getActiveCount());
            assertEquals(0, wrapper.getExecutor().getLargestPoolSize());
            assertTrue(getStopWatchMap(wrapper).isEmpty());

            decorated.run();

            assertEquals(1, wrapper.getExecutor().getTaskCount());
            assertEquals(1, wrapper.getExecutor().getCompletedTaskCount());
            assertEquals(0, wrapper.getExecutor().getActiveCount());
            assertEquals(1, wrapper.getExecutor().getLargestPoolSize());
            assertTrue(getStopWatchMap(wrapper).isEmpty());
        } finally {
            proxy.shutdownNow();
        }
    }

    @Test
    void taskRejectedBySpringAfterDecorationDoesNotLeakCounters() throws Exception {
        assumeJdk21Plus();

        SimpleAsyncTaskExecutor simple = new SimpleAsyncTaskExecutor("limited-vt-");
        simple.setVirtualThreads(true);
        simple.setConcurrencyLimit(1);
        // SimpleAsyncTaskExecutor#execute applies the TaskDecorator first and only then hits
        // the concurrency throttle, i.e. the task is rejected after dtp decorated it
        simple.setRejectTasksWhenLimitReached(true);

        DtpPostProcessor processor = new DtpPostProcessor();
        processor.setBeanFactory(new DefaultListableBeanFactory());
        processor.setEnvironment(new MockEnvironment()
                .withProperty("spring.threads.virtual.enabled", "true"));
        processor.postProcessAfterInitialization(simple, POOL_NAME);

        ExecutorWrapper wrapper = DtpRegistry.getExecutorWrapper(POOL_NAME);
        AwareManager.register(wrapper);

        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch started = new CountDownLatch(1);
        simple.execute(() -> {
            started.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertTrue(started.await(5, TimeUnit.SECONDS));

        assertThrows(TaskRejectedException.class, () -> simple.execute(() -> { }));

        // only the running task is accounted for, the rejected one left no residue
        assertEquals(1, wrapper.getExecutor().getTaskCount());
        assertEquals(1, wrapper.getExecutor().getActiveCount());
        assertEquals(1, getStopWatchMap(wrapper).size());

        release.countDown();
        waitUntil(() -> wrapper.getExecutor().getCompletedTaskCount() == 1);
        assertEquals(0, wrapper.getExecutor().getActiveCount(),
                "active count must not drift when a decorated task gets rejected");
        // completedTaskCount is incremented after afterExecute cleared the performance key
        assertTrue(getStopWatchMap(wrapper).isEmpty(), "performance keys must not leak");
        simple.close();
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
            assertEquals(0, captured.getExecutor().getActiveCount());
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

    @Test
    void factoryWorksInJvmWithoutAddOpens() throws Exception {
        assumeJdk21Plus();

        // Surefire runs with --add-opens=java.base/java.lang=ALL-UNNAMED, which hides
        // module access problems that a plain application JVM would hit. So fork a clean
        // JVM without any --add-opens to guard against reflection on the non-public
        // java.lang.ThreadBuilders$VirtualThreadBuilder implementation class.
        String java = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
        Process process = new ProcessBuilder(java,
                "-cp", System.getProperty("java.class.path"),
                VirtualThreadProbe.class.getName())
                .redirectErrorStream(true)
                .start();
        String output;
        try (InputStream in = process.getInputStream()) {
            output = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertTrue(process.waitFor(60, TimeUnit.SECONDS));
        assertEquals(0, process.exitValue(), "probe JVM failed: " + output);
        assertTrue(output.contains("VIRTUAL_OK:true"), "unexpected probe output: " + output);
    }

    /**
     * Entry point executed in a forked JVM by {@link #factoryWorksInJvmWithoutAddOpens()}.
     */
    static class VirtualThreadProbe {

        public static void main(String[] args) throws Exception {
            ExecutorService executor = VirtualThreadExecutorFactory.newThreadPerTaskExecutor("vt-probe-");
            try {
                AtomicBoolean isVirtual = new AtomicBoolean(false);
                CountDownLatch latch = new CountDownLatch(1);
                executor.execute(() -> {
                    isVirtual.set(runningOnVirtualThread());
                    latch.countDown();
                });
                if (!latch.await(15, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("task did not run");
                }
                System.out.println("VIRTUAL_OK:" + isVirtual.get());
            } finally {
                executor.shutdownNow();
            }
        }

        /**
         * {@code Thread#isVirtual()} is JDK 21 API while this module compiles with release 17,
         * so it has to be called reflectively.
         */
        private static boolean runningOnVirtualThread() {
            try {
                return (Boolean) Thread.class.getMethod("isVirtual").invoke(Thread.currentThread());
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Test
    void rejectedTaskClosesAwareChainAndCountsRejection() throws Exception {
        // delegate that rejects: a shut down ThreadPoolExecutor behaves like a shut down
        // JDK thread-per-task executor (RejectedExecutionException from execute).
        ExecutorService delegate = java.util.concurrent.Executors.newSingleThreadExecutor();
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(delegate);
        ExecutorWrapper wrapper = new ExecutorWrapper(POOL_NAME, proxy);
        DtpRegistry.registerExecutor(wrapper, "test");
        AwareManager.register(wrapper);
        wrapper.getThreadPoolStatProvider().setQueueTimeout(50);

        delegate.shutdown();
        assertThrows(RejectedExecutionException.class, () -> proxy.execute(() -> { }));

        // no leaked performance key, no armed queue-timeout timer, rejection counted
        assertTrue(getStopWatchMap(wrapper).isEmpty(), "stopWatchMap must not leak on reject");
        assertTrue(getMap(wrapper, "queueTimeoutMap").isEmpty(), "queueTimeoutMap must not leak on reject");
        assertEquals(1, wrapper.getThreadPoolStatProvider().getRejectedTaskCount());

        // and no false queue timeout is reported afterwards
        Thread.sleep(300);
        assertEquals(0, wrapper.getThreadPoolStatProvider().getQueueTimeoutCount());
    }

    @Test
    void queueTimeoutTimerIsNotArmedForVirtualExecutor() throws Exception {
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(
                java.util.concurrent.Executors.newSingleThreadExecutor());
        ExecutorWrapper wrapper = new ExecutorWrapper(POOL_NAME, proxy);
        DtpRegistry.registerExecutor(wrapper, "test");
        AwareManager.register(wrapper);
        wrapper.getThreadPoolStatProvider().setQueueTimeout(10_000);
        try {
            wrapper.getThreadPoolStatProvider().startQueueTimeoutTask(() -> { });
            assertTrue(getMap(wrapper, "queueTimeoutMap").isEmpty(),
                    "virtual thread executors have no queue, timer must not be armed");
        } finally {
            proxy.shutdownNow();
        }
    }

    @Test
    void shutdownNotifiesAwareChainOnce() {
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(
                java.util.concurrent.Executors.newSingleThreadExecutor());
        ExecutorWrapper wrapper = new ExecutorWrapper(POOL_NAME, proxy);
        DtpRegistry.registerExecutor(wrapper, "test");

        AtomicInteger shutdownCount = new AtomicInteger();
        AtomicInteger terminatedCount = new AtomicInteger();
        ExecutorAware aware = new ExecutorAware() {
            @Override
            public int getOrder() {
                return 0;
            }

            @Override
            public String getName() {
                return "vt-shutdown-probe";
            }

            @Override
            public void shutdown(java.util.concurrent.Executor executor) {
                shutdownCount.incrementAndGet();
            }

            @Override
            public void terminated(java.util.concurrent.Executor executor) {
                terminatedCount.incrementAndGet();
            }
        };
        AwareManager.add(aware);
        try {
            proxy.shutdown();
            assertEquals(1, shutdownCount.get());
            assertTrue(proxy.awaitTermination(3, TimeUnit.SECONDS));
            // terminated must be reported exactly once, even across repeated calls
            assertTrue(proxy.awaitTermination(1, TimeUnit.SECONDS));
            proxy.shutdown();
            assertEquals(1, terminatedCount.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        } finally {
            removeAware(aware);
        }
    }

    @Test
    void genericConstructorAcceptsVirtualProxyHeldAsExecutor() {
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(
                java.util.concurrent.Executors.newSingleThreadExecutor());
        // static type Executor: the dedicated overload is not selected at compile time
        Executor asExecutor = proxy;
        try {
            ExecutorWrapper wrapper = new ExecutorWrapper(POOL_NAME, asExecutor);
            assertTrue(wrapper.isVirtualThreadExecutor());
            assertSame(proxy, wrapper.getExecutor().getOriginal());
        } finally {
            proxy.shutdownNow();
        }
    }

    @Test
    void refreshSyncsMetadataBackToProxy() {
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(
                java.util.concurrent.Executors.newSingleThreadExecutor());
        proxy.setThreadPoolAliasName("old-alias");

        DtpPostProcessor processor = new DtpPostProcessor();
        processor.setBeanFactory(new DefaultListableBeanFactory());
        processor.setEnvironment(new MockEnvironment());
        processor.postProcessAfterInitialization(proxy, POOL_NAME);

        DtpExecutorProps props = new DtpExecutorProps();
        props.setThreadPoolName(POOL_NAME);
        props.setThreadPoolAliasName("synced-alias");
        props.setRunTimeout(1200);
        props.setQueueTimeout(2400);
        props.setTryInterrupt(true);
        props.setNotifyEnabled(false);
        props.setAwaitTerminationSeconds(9);
        props.setWaitForTasksToCompleteOnShutdown(true);
        props.setRejectedHandlerType("AbortPolicy");

        DtpRegistry.refresh(props);

        assertEquals(POOL_NAME, proxy.getThreadPoolName());
        assertEquals("synced-alias", proxy.getThreadPoolAliasName());
        assertEquals(1200, proxy.getRunTimeout());
        assertEquals(2400, proxy.getQueueTimeout());
        assertTrue(proxy.isTryInterrupt());
        assertFalse(proxy.isNotifyEnabled());
        assertEquals(9, proxy.getAwaitTerminationSeconds());
        assertTrue(proxy.isWaitForTasksToCompleteOnShutdown());

        proxy.shutdownNow();
    }

    @Test
    void metricsFlagVirtualAndSkipPoolGauges() {
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(
                java.util.concurrent.Executors.newSingleThreadExecutor());
        ExecutorWrapper wrapper = new ExecutorWrapper(POOL_NAME, proxy);
        DtpRegistry.registerExecutor(wrapper, "test");
        try {
            ThreadPoolStats stats = ExecutorConverter.toMetrics(wrapper);
            assertTrue(stats.isVirtual());
            assertEquals(VirtualThreadExecutorAdapter.QUEUE_TYPE, stats.getQueueType());

            SimpleMeterRegistry registry = new SimpleMeterRegistry();
            Metrics.addRegistry(registry);
            try {
                new MicroMeterCollector().collect(stats);
                assertNull(registry.find(MicroMeterCollector.DTP_METRIC_NAME_PREFIX + ".core.size").gauge(),
                        "sizing gauges must not be reported for virtual thread executors");
                assertNull(registry.find(MicroMeterCollector.DTP_METRIC_NAME_PREFIX + ".queue.size").gauge());
                assertNotNull(registry.find(MicroMeterCollector.DTP_METRIC_NAME_PREFIX + ".task.count").gauge(),
                        "task statistics must be reported");
                assertNotNull(registry.find(MicroMeterCollector.DTP_METRIC_NAME_PREFIX + ".active.count").gauge());
                assertNotNull(registry.find(MicroMeterCollector.DTP_METRIC_NAME_PREFIX + ".tps").gauge(),
                        "task performance gauges must still be reported");
            } finally {
                Metrics.removeRegistry(registry);
                registry.close();
            }
        } finally {
            proxy.shutdownNow();
        }
    }

    private static void waitUntil(java.util.function.BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
    }

    private static void removeAware(ExecutorAware aware) {
        try {
            Field field = AwareManager.class.getDeclaredField("EXECUTOR_AWARE_LIST");
            field.setAccessible(true);
            ((List<?>) field.get(null)).remove(aware);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private Map<?, ?> getMap(ExecutorWrapper wrapper, String name) throws Exception {
        Field field = wrapper.getThreadPoolStatProvider().getClass().getDeclaredField(name);
        field.setAccessible(true);
        return (Map<?, ?>) field.get(wrapper.getThreadPoolStatProvider());
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
