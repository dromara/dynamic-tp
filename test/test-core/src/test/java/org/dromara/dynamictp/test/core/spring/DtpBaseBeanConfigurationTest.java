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

import org.dromara.dynamictp.common.ApplicationContextHolder;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.monitor.DtpMonitor;
import org.dromara.dynamictp.core.spring.DtpBaseBeanConfiguration;
import org.dromara.dynamictp.core.spring.DtpPostProcessor;
import org.dromara.dynamictp.core.spring.EnableDynamicTp;
import org.dromara.dynamictp.core.spring.YamlPropertySourceFactory;
import org.dromara.dynamictp.core.support.DtpBannerPrinter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;

/**
 * DtpBaseBeanConfigurationTest related
 *
 * @author KamTo Hung
 * @since 1.1.1
 */
@PropertySource(value = "classpath:/demo-dtp-dev.yml", factory = YamlPropertySourceFactory.class)
@SpringBootTest(classes = DtpBaseBeanConfigurationTest.class)
public class DtpBaseBeanConfigurationTest {

    @SpringBootTest(classes = DtpBaseBeanConfigurationTest.class)
    @EnableDynamicTp
    public static class EnableDynamicTpAnnotationTest {

        @Autowired
        private ApplicationContext applicationContext;

        @Test
        void test() {
            Assertions.assertNotNull(applicationContext.getBean(DtpBaseBeanConfiguration.class));
            Assertions.assertNotNull(applicationContext.getBean(ApplicationContextHolder.class));
            Assertions.assertNotNull(applicationContext.getBean(DtpBannerPrinter.class));
            Assertions.assertNotNull(applicationContext.getBean(DtpPostProcessor.class));
            Assertions.assertNotNull(applicationContext.getBean(DtpRegistry.class));
            Assertions.assertNotNull(applicationContext.getBean(DtpMonitor.class));
        }

    }

    @SpringBootTest(classes = DtpBaseBeanConfigurationTest.class)
    public static class DisableDynamicTpAnnotationTest {

        @Autowired
        private ApplicationContext applicationContext;

        @Test
        void test() {
            Assertions.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(DtpBaseBeanConfiguration.class));
            Assertions.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(ApplicationContextHolder.class));
            Assertions.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(DtpBannerPrinter.class));
            Assertions.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(DtpPostProcessor.class));
            Assertions.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(DtpRegistry.class));
            Assertions.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(DtpMonitor.class));
        }

    }

}
