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
import org.dromara.dynamictp.core.support.proxy.ThreadPoolExecutorProxy;
import org.dromara.dynamictp.spring.annotation.EnableDynamicTp;
import org.dromara.dynamictp.spring.support.YamlPropertySourceFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
@EnableDynamicTp
@EnableAutoConfiguration
@PropertySource(value = "classpath:/postprocessor-dtp-dev.yml", factory = YamlPropertySourceFactory.class)
@ComponentScan(basePackages = "org.dromara.dynamictp.test.core.spring")
public class DtpPostProcessorTest {

    private static ConfigurableApplicationContext context;

    @BeforeAll
    public static void setUp() {
        context = SpringApplication.run(DtpPostProcessorTest.class);
    }

    @Test
    void test() {
        Executor executor = DtpRegistry.getExecutor("asyncExecutor");
        Assertions.assertNotNull(executor);

        Executor commonExecutor = context.getBean("commonExecutor", ThreadPoolExecutor.class);
        Assertions.assertEquals(commonExecutor.getClass(), ThreadPoolExecutorProxy.class);
        commonExecutor.execute(() -> System.out.println("enhance commonExecutor success!"));

        ThreadPoolTaskExecutor taskExecutor = context.getBean("taskExecutor", ThreadPoolTaskExecutor.class);
        Assertions.assertEquals(taskExecutor.getThreadPoolExecutor().getClass(), ThreadPoolExecutorProxy.class);
        taskExecutor.execute(() -> System.out.println("enhance taskExecutor success!"));
    }

}
