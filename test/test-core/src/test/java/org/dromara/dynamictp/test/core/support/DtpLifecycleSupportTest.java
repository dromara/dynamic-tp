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
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.support.DtpLifecycleSupport;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.spring.YamlPropertySourceFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * DtpLifecycleSupportTest related
 *
 * @author vzer200
 * @since 1.1.8
 */
@PropertySource(value = "classpath:/demo-dtp-dev.yml", factory = YamlPropertySourceFactory.class)
@SpringBootTest(classes = DtpLifecycleSupportTest.class)
@EnableAutoConfiguration
@ComponentScan(basePackages = "org.dromara.dynamictp.test.core.spring")
public class DtpLifecycleSupportTest {

    private ExecutorWrapper executorWrapper;
    private DtpExecutor dtpExecutor;

    @BeforeEach
    public void setUp() {
        dtpExecutor = (DtpExecutor) DtpRegistry.getExecutor("dtpExecutor1");
        if (dtpExecutor == null) {
            throw new RuntimeException("dtpExecutor1 not found!");
        }
        executorWrapper = new ExecutorWrapper(dtpExecutor);
    }

    @Test
    public void testInitialize() {
        int initialCorePoolSize = dtpExecutor.getCorePoolSize();
        int initialMaxPoolSize = dtpExecutor.getMaximumPoolSize();

        DtpLifecycleSupport.initialize(executorWrapper);

        Assertions.assertEquals(initialCorePoolSize, dtpExecutor.getCorePoolSize());
        Assertions.assertEquals(initialMaxPoolSize, dtpExecutor.getMaximumPoolSize());
        Assertions.assertTrue(dtpExecutor.isNotifyEnabled());
    }


    @Test
    public void testDestroy() throws InterruptedException {
        DtpLifecycleSupport.initialize(executorWrapper);

        DtpLifecycleSupport.destroy(executorWrapper);

        Assertions.assertTrue(dtpExecutor.isShutdown());
        Assertions.assertTrue(dtpExecutor.awaitTermination(1, TimeUnit.SECONDS));
    }

    @Test
    public void testRejectHandlerEnhancement() {
        // 手动创建一个小容量的线程池
        DtpExecutor smallExecutor = new DtpExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1));

        smallExecutor.execute(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        smallExecutor.execute(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 第三个任务应该被拒绝，触发拒绝策略
        Assertions.assertThrows(RejectedExecutionException.class, () -> {
            smallExecutor.execute(() -> System.out.println("This task should be rejected"));
        });
    }


}
