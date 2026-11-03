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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;

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

        Config.FieldExecutorHolder holder = context.getBean(Config.FieldExecutorHolder.class);
        ExecutorWrapper fieldExecutor = DtpRegistry.getExecutorWrapper("fieldExecutor");
        assertSame(holder.getExecutor(), fieldExecutor.getExecutor().getOriginal());
        assertFalse(holder.getExecutor().isShutdown());
        assertFalse(fieldExecutor.getExecutor().getOriginal() instanceof ThreadPoolExecutorProxy);
    }

    @Test
    void registerExecutorIfAbsentAvoidsDuplicateInstancesAndNameConflicts() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
        ThreadPoolExecutor conflictingExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
        try {
            assertTrue(DtpRegistry.registerExecutorIfAbsent(
                    "fieldExecutorRegistrationTest", executor, "test"));
            assertFalse(DtpRegistry.registerExecutorIfAbsent(
                    "fieldExecutorAliasTest", executor, "test"));
            assertFalse(DtpRegistry.registerExecutorIfAbsent(
                    "fieldExecutorRegistrationTest", conflictingExecutor, "test"));
            assertSame(executor, DtpRegistry.getExecutorWrapper("fieldExecutorRegistrationTest")
                    .getExecutor().getOriginal());
        } finally {
            DtpRegistry.unregisterExecutor("fieldExecutorRegistrationTest");
            executor.shutdownNow();
            conflictingExecutor.shutdownNow();
        }
    }

}
