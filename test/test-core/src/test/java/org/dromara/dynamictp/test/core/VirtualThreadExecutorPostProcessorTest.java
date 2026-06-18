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

import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.proxy.VirtualThreadExecutorProxy;
import org.dromara.dynamictp.spring.DtpPostProcessor;
import org.dromara.dynamictp.spring.annotation.DtpBeanDefinitionRegistrar;
import org.dromara.dynamictp.spring.holder.SpringContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.mock.env.MockEnvironment;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * VirtualThreadExecutorPostProcessorTest related
 *
 * @author yanhom
 * @since 1.x.x
 */
@Execution(ExecutionMode.SAME_THREAD)
class VirtualThreadExecutorPostProcessorTest {

    private static final String VIRTUAL_PROXY_NAME = "testVirtualProxy";

    private static final String SIMPLE_ASYNC_NAME = "testSimpleAsyncVirtual";

    private static final String CONFIGURED_VIRTUAL_NAME = "configuredVirtual";

    private GenericApplicationContext context;

    private ApplicationContext originalContext;

    @BeforeEach
    void setUp() throws Exception {
        originalContext = springContext();
        context = new GenericApplicationContext();
        context.refresh();
        new SpringContextHolder().setApplicationContext(context);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (DtpRegistry.getAllExecutorNames().contains(VIRTUAL_PROXY_NAME)) {
            DtpRegistry.unregisterExecutor(VIRTUAL_PROXY_NAME);
        }
        if (DtpRegistry.getAllExecutorNames().contains(SIMPLE_ASYNC_NAME)) {
            DtpRegistry.unregisterExecutor(SIMPLE_ASYNC_NAME);
        }
        if (DtpRegistry.getAllExecutorNames().contains(CONFIGURED_VIRTUAL_NAME)) {
            DtpRegistry.unregisterExecutor(CONFIGURED_VIRTUAL_NAME);
        }
        DtpProperties.getInstance().setExecutors(null);
        context.close();
        setSpringContext(originalContext);
    }

    @Test
    void registerVirtualThreadExecutorProxyBean() {
        DtpPostProcessor processor = newPostProcessor(false);
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(Executors.newSingleThreadExecutor());

        Object result = processor.postProcessAfterInitialization(proxy, VIRTUAL_PROXY_NAME);

        assertSame(proxy, result);
        assertTrue(DtpRegistry.getExecutorWrapper(VIRTUAL_PROXY_NAME).isVirtualThreadExecutor());
    }

    @Test
    void registrarAddsCommonPropertyValuesForVirtualExecutor() {
        DtpBeanDefinitionRegistrar registrar = new DtpBeanDefinitionRegistrar();
        registrar.setEnvironment(new MockEnvironment()
                .withProperty("dynamictp.executors[0].threadPoolName", CONFIGURED_VIRTUAL_NAME)
                .withProperty("dynamictp.executors[0].threadPoolAliasName", "configuredAlias")
                .withProperty("dynamictp.executors[0].executorType", "virtual")
                .withProperty("dynamictp.executors[0].notifyEnabled", "false")
                .withProperty("dynamictp.executors[0].rejectEnhanced", "false")
                .withProperty("dynamictp.executors[0].runTimeout", "1000")
                .withProperty("dynamictp.executors[0].queueTimeout", "2000")
                .withProperty("dynamictp.executors[0].tryInterrupt", "true")
                .withProperty("dynamictp.executors[0].awaitTerminationSeconds", "9"));
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();

        registrar.registerBeanDefinitions(AnnotationMetadata.introspect(getClass()), registry);

        BeanDefinition beanDefinition = registry.getBeanDefinition(CONFIGURED_VIRTUAL_NAME);
        assertEquals(VirtualThreadExecutorProxy.class.getName(), beanDefinition.getBeanClassName());
        assertEquals("configuredAlias", beanDefinition.getPropertyValues()
                .getPropertyValue("threadPoolAliasName").getValue());
        assertEquals(1000L, beanDefinition.getPropertyValues().getPropertyValue("runTimeout").getValue());
        assertEquals(2000L, beanDefinition.getPropertyValues().getPropertyValue("queueTimeout").getValue());
        assertTrue(beanDefinition.getPropertyValues().contains("notifyItems"));
        assertTrue(!beanDefinition.getPropertyValues().contains("allowCoreThreadTimeOut"));
        assertTrue(!beanDefinition.getPropertyValues().contains("preStartAllCoreThreads"));
    }

    @Test
    void virtualThreadExecutorProxyUsesBoundPropertiesForInitialConfiguration() {
        DtpPostProcessor processor = newPostProcessor(false);
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(Executors.newSingleThreadExecutor());
        proxy.setThreadPoolAliasName("virtualAlias");
        proxy.setPlatformIds(Collections.singletonList("platform"));
        proxy.setNotifyEnabled(false);
        proxy.setRejectEnhanced(false);
        proxy.setWaitForTasksToCompleteOnShutdown(true);
        proxy.setAwaitTerminationSeconds(9);
        proxy.setRunTimeout(1000);
        proxy.setQueueTimeout(2000);
        proxy.setTryInterrupt(true);

        processor.postProcessAfterInitialization(proxy, VIRTUAL_PROXY_NAME);

        ExecutorWrapper wrapper = DtpRegistry.getExecutorWrapper(VIRTUAL_PROXY_NAME);
        assertEquals("virtualAlias", wrapper.getThreadPoolAliasName());
        assertEquals(Collections.singletonList("platform"), wrapper.getPlatformIds());
        assertEquals(9, wrapper.getAwaitTerminationSeconds());
        assertEquals(1000, wrapper.getThreadPoolStatProvider().getRunTimeout());
        assertEquals(2000, wrapper.getThreadPoolStatProvider().getQueueTimeout());
        assertTrue(wrapper.isWaitForTasksToCompleteOnShutdown());
        assertTrue(wrapper.getThreadPoolStatProvider().isTryInterrupt());
        assertTrue(!wrapper.isNotifyEnabled());
        assertTrue(!wrapper.isRejectEnhanced());
    }

    @Test
    void simpleAsyncTaskExecutorRunsThroughRegisteredVirtualProxy() throws Exception {
        DtpPostProcessor processor = newPostProcessor(true);
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        AtomicBoolean enhanced = new AtomicBoolean();
        CountDownLatch latch = new CountDownLatch(1);

        processor.postProcessAfterInitialization(executor, SIMPLE_ASYNC_NAME);
        ExecutorWrapper wrapper = DtpRegistry.getExecutorWrapper(SIMPLE_ASYNC_NAME);
        wrapper.setTaskWrappers(java.util.Collections.singletonList(runnable -> () -> {
            enhanced.set(true);
            runnable.run();
        }));

        executor.execute(latch::countDown);

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertTrue(enhanced.get());
    }

    @Test
    void virtualThreadExecutorProxyEnhancesBulkSubmission() throws Exception {
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(Executors.newFixedThreadPool(2));
        AtomicInteger enhancedCount = new AtomicInteger();
        proxy.setTaskWrappers(java.util.Collections.singletonList(runnable -> () -> {
            enhancedCount.incrementAndGet();
            runnable.run();
        }));

        try {
            List<Future<Integer>> futures = proxy.invokeAll(Arrays.asList(() -> 1, () -> 2));
            assertEquals(3, futures.get(0).get() + futures.get(1).get());

            List<Callable<Boolean>> tasks = Arrays.asList(() -> true, () -> true);
            assertTrue(proxy.invokeAny(tasks));
            assertTrue(enhancedCount.get() >= 3);
        } finally {
            proxy.shutdownNow();
        }
    }

    private DtpPostProcessor newPostProcessor(boolean virtualEnabled) {
        DtpPostProcessor processor = new DtpPostProcessor();
        processor.setBeanFactory(new DefaultListableBeanFactory());
        processor.setEnvironment(new MockEnvironment()
                .withProperty("spring.threads.virtual.enabled", Boolean.toString(virtualEnabled)));
        return processor;
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
