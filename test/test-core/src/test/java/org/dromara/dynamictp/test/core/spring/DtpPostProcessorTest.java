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

package org.dromara.dynamictp.test.core.spring;

import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.proxy.ThreadPoolExecutorProxy;
import org.dromara.dynamictp.spring.annotation.EnableDynamicTp;
import org.dromara.dynamictp.spring.support.YamlPropertySourceFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
@EnableDynamicTp
@EnableAutoConfiguration
@PropertySource(value = "classpath:/postprocessor-dtp-dev.yml", factory = YamlPropertySourceFactory.class)
@ComponentScan(basePackages = "org.dromara.dynamictp.test.core.spring")
@SpringBootTest(classes = DtpPostProcessorTest.class)
@Execution(SAME_THREAD)
@ResourceLock("DTP_REGISTRY")
class DtpPostProcessorTest {

    @Autowired
    private ConfigurableApplicationContext context;

    @Test
    void test() {
        Executor executor = DtpRegistry.getExecutor("asyncExecutor");
        assertNotNull(executor);

        Executor commonExecutor = context.getBean("commonExecutor", ThreadPoolExecutor.class);
        assertEquals(ThreadPoolExecutorProxy.class, commonExecutor.getClass());
        commonExecutor.execute(() -> System.out.println("enhance commonExecutor success!"));

        ThreadPoolTaskExecutor taskExecutor = context.getBean("taskExecutor", ThreadPoolTaskExecutor.class);
        assertEquals(ThreadPoolExecutorProxy.class, taskExecutor.getThreadPoolExecutor().getClass());
        taskExecutor.execute(() -> System.out.println("enhance taskExecutor success!"));
    }

    /**
     * dtp replaces the decorating executor created by {@link ThreadPoolTaskExecutor} and shuts it
     * down, so the bean's {@code TaskDecorator} only survives as a dtp task wrapper. A config
     * refresh resets the wrappers to the ones named in {@code taskWrapperNames} (see
     * {@code DtpRegistry#doRefreshCommon}), which must not drop it.
     */
    @Test
    void taskDecoratorOfWrappedBeanSurvivesWrapperReset() throws Exception {
        ThreadPoolTaskExecutor executor = context.getBean("decoratedTaskExecutor", ThreadPoolTaskExecutor.class);
        ExecutorWrapper wrapper = DtpRegistry.getExecutorWrapper("decoratedTaskExecutor");

        Config.DECORATOR_CALLS.set(0);
        runAndWait(executor);
        assertEquals(1, Config.DECORATOR_CALLS.get(), "the bean's TaskDecorator must be applied");

        // exactly what a config refresh does when no taskWrapperNames are configured
        wrapper.setTaskWrappers(Collections.emptyList());

        Config.DECORATOR_CALLS.set(0);
        runAndWait(executor);
        assertEquals(1, Config.DECORATOR_CALLS.get(),
                "the TaskDecorator taken over from the bean must not be dropped by a refresh");
    }

    private void runAndWait(ThreadPoolTaskExecutor executor) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        executor.execute(latch::countDown);
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

}
