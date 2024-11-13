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
import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.notifier.manager.NotifyHelper;
import org.dromara.dynamictp.core.support.DtpLifecycleSupport;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.spring.support.YamlPropertySourceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.Executor;

@SpringBootTest(classes = DtpLifecycleSupportTest.TestConfig.class)
@ComponentScan(basePackages = "org.dromara.dynamictp.test.core.spring")
public class DtpLifecycleSupportTest {

    private ExecutorWrapper executorWrapper;
    private DtpExecutor dtpExecutor;

    @BeforeEach
    public void setUp() {
        Executor executor = DtpRegistry.getExecutor("dtpExecutor1");
        if (executor instanceof DtpExecutor) {
            dtpExecutor = Mockito.spy((DtpExecutor) executor);
        } else {
            throw new RuntimeException("dtpExecutor1 is not of type DtpExecutor!");
        }
        executorWrapper = new ExecutorWrapper(dtpExecutor);
    }

    @Test
    public void testInitialize() {
        try (MockedStatic<AwareManager> awareManagerMockedStatic = Mockito.mockStatic(AwareManager.class)) {
            awareManagerMockedStatic.when(() -> AwareManager.register(Mockito.any(ExecutorWrapper.class)))
                    .thenAnswer(invocation -> null);

            try (MockedStatic<NotifyHelper> notifyHelperMockedStatic = Mockito.mockStatic(NotifyHelper.class)) {
                notifyHelperMockedStatic.when(() -> NotifyHelper.initNotify((DtpExecutor) executorWrapper.getExecutor()))
                        .thenAnswer(invocation -> null);

                DtpLifecycleSupport.initialize(executorWrapper);

                // 验证初始化
                Mockito.verify(dtpExecutor).initialize();
                awareManagerMockedStatic.verify(() -> AwareManager.register(executorWrapper));
                notifyHelperMockedStatic.verify(() -> NotifyHelper.initNotify((DtpExecutor) executorWrapper.getExecutor()));

                // 预启动核心线程的验证
                if (dtpExecutor.isPreStartAllCoreThreads()) {
                    Mockito.verify(dtpExecutor).prestartAllCoreThreads();
                }

            }
        }
    }

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = "org.dromara.dynamictp.test.core.spring")
    @PropertySource(value = "classpath:/demo-dtp-dev.yml", factory = YamlPropertySourceFactory.class)
    public static class TestConfig {
        @Bean
        public DtpExecutor dtpExecutor() {
            // 从 DtpRegistry 获取并确保类型正确
            Executor executor = DtpRegistry.getExecutor("dtpExecutor1");
            if (executor instanceof DtpExecutor) {
                return (DtpExecutor) executor;
            }
            throw new RuntimeException("dtpExecutor1 is not of type DtpExecutor!");
        }
    }
}
