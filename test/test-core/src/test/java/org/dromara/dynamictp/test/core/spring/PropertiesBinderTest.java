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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.support.binder.BinderHelper;
import org.dromara.dynamictp.spring.annotation.EnableDynamicTp;
import org.dromara.dynamictp.spring.support.YamlPropertySourceFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.AbstractEnvironment;

import java.util.List;
import java.util.Map;

/**
 * PropertiesBinderTest related
 *
 * @author yanhom
 * @since 1.1.0
 */
@PropertySource(value = "classpath:/demo-dtp-dev.yml",
        factory = YamlPropertySourceFactory.class)
@SpringBootTest(classes = PropertiesBinderTest.class)
@EnableAutoConfiguration
@EnableDynamicTp
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 使用同一个实例运行所有测试
class PropertiesBinderTest {

    @Autowired
    private AbstractEnvironment environment;

    @Test
    void testBindDtpPropertiesWithMap() throws Exception {
        try {
            Map<Object, Object> properties = Maps.newHashMap();
            properties.put("dynamictp.enabled", false);
            properties.put("dynamictp.collectorTypes", Lists.newArrayList("LOGGING"));
            properties.put("dynamictp.executors[0].threadPoolName", "test_dtp");
            properties.put("dynamictp.executors[1].threadPoolName", "test_dtp1");
            properties.put("dynamictp.executors[0].executorType", "common");
            properties.put("dynamictp.globalExecutorProps.executorType", "eager");

            DtpProperties dtpProperties = DtpProperties.getInstance();
            System.out.println("Collector Types before binding: " + dtpProperties.getCollectorTypes());
            BinderHelper.bindDtpProperties(properties, dtpProperties);
            System.out.println("Collector Types after binding: " + dtpProperties.getCollectorTypes());

            Assertions.assertEquals(properties.get("dynamictp.executors[0].threadPoolName"),
                    dtpProperties.getExecutors().get(0).getThreadPoolName());
            Assertions.assertIterableEquals((List<String>) properties.get("dynamictp.collectorTypes"),
                    dtpProperties.getCollectorTypes());
            Assertions.assertEquals("common",
                    dtpProperties.getExecutors().get(0).getExecutorType());
            Assertions.assertEquals(properties.get("dynamictp.globalExecutorProps.executorType"),
                    dtpProperties.getExecutors().get(1).getExecutorType());

        } catch (Exception e) {
            throw new RuntimeException("Failed to reset DtpProperties instance", e);
        }
    }

    @Test
    void testBindDtpPropertiesWithEnvironment() {
        DtpProperties dtpProperties = DtpProperties.getInstance();
        BinderHelper.bindDtpProperties(environment, dtpProperties);
        String threadPoolName = environment.getProperty("dynamictp.executors[0].threadPoolName");
        Assertions.assertEquals(threadPoolName, dtpProperties.getExecutors().get(0).getThreadPoolName());
        String executorType = environment.getProperty("dynamictp.globalExecutorProps.executorType");
        Assertions.assertEquals(executorType, dtpProperties.getExecutors().get(1).getExecutorType());

    }

}
