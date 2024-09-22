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
import org.dromara.dynamictp.core.spring.YamlPropertySourceFactory;
import org.dromara.dynamictp.core.support.BinderHelper;
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PropertiesBinderTest {

    @Autowired
    private AbstractEnvironment environment;

    @Test
    void testBindDtpPropertiesWithMap() {
        Map<Object, Object> properties  = Maps.newHashMap();
        properties.put("spring.dynamic.tp.enabled", false);
        properties.put("spring.dynamic.tp.collectorTypes", Lists.newArrayList("LOGGING"));
        properties.put("spring.dynamic.tp.executors[0].threadPoolName", "test_dtp");
        properties.put("spring.dynamic.tp.executors[1].threadPoolName", "test_dtp1");
        properties.put("spring.dynamic.tp.executors[0].executorType", "common");
        properties.put("spring.dynamic.tp.globalExecutorProps.executorType","eager");

        DtpProperties dtpProperties = DtpProperties.getInstance();
        BinderHelper.bindDtpProperties(properties, dtpProperties);
        Assertions.assertEquals(properties.get("spring.dynamic.tp.executors[0].threadPoolName"),
                dtpProperties.getExecutors().get(0).getThreadPoolName());
        Assertions.assertIterableEquals((List<String>) properties.get("spring.dynamic.tp.collectorTypes"),
                dtpProperties.getCollectorTypes());
        Assertions.assertEquals("common",
                dtpProperties.getExecutors().get(0).getExecutorType());
        Assertions.assertEquals(properties.get("spring.dynamic.tp.globalExecutorProps.executorType"),
                dtpProperties.getExecutors().get(1).getExecutorType());
    }

    @Test
    void testBindDtpPropertiesWithEnvironment() {
        DtpProperties dtpProperties = DtpProperties.getInstance();
        BinderHelper.bindDtpProperties(environment, dtpProperties);
        String threadPoolName = environment.getProperty("spring.dynamic.tp.executors[0].threadPoolName");
        Assertions.assertEquals(threadPoolName, dtpProperties.getExecutors().get(0).getThreadPoolName());
        String executorType = environment.getProperty("spring.dynamic.tp.globalExecutorProps.executorType");
        Assertions.assertEquals(executorType, dtpProperties.getExecutors().get(1).getExecutorType());
    }

    @Test
    void testResetDtpWithGlobalConfig() {
        DtpProperties dtpProperties = DtpProperties.getInstance();
        BinderHelper.bindDtpProperties(environment, dtpProperties);

        Assertions.assertEquals(1, dtpProperties.getExecutors().get(1).getCorePoolSize());
        Assertions.assertEquals(18, dtpProperties.getExecutors().get(1).getMaximumPoolSize());
        Assertions.assertEquals(201, dtpProperties.getExecutors().get(1).getRunTimeout());
        Assertions.assertEquals(80, dtpProperties.getExecutors().get(0).getNotifyItems().get(0).getThreshold());
        Assertions.assertEquals(81, dtpProperties.getExecutors().get(1).getNotifyItems().get(0).getThreshold());
    }

    @Test
    void testResetAdapterTpWithGlobalConfig() {
        DtpProperties dtpProperties = DtpProperties.getInstance();
        BinderHelper.bindDtpProperties(environment, dtpProperties);

        Assertions.assertEquals(400, dtpProperties.getUndertowTp().getMaximumPoolSize());
        Assertions.assertEquals(201, dtpProperties.getTomcatTp().getRunTimeout());
        Assertions.assertEquals(101, dtpProperties.getTomcatTp().getQueueTimeout());
        Assertions.assertIterableEquals(Lists.newArrayList("ttl"), dtpProperties.getTomcatTp().getTaskWrapperNames());
        Assertions.assertEquals(5, dtpProperties.getTomcatTp().getNotifyItems().size());

        Assertions.assertEquals(201, dtpProperties.getRocketMqTp().get(0).getRunTimeout());
        Assertions.assertEquals(1, dtpProperties.getRocketMqTp().get(1).getCorePoolSize());

        Assertions.assertEquals(81, dtpProperties.getRocketMqTp().get(0).getNotifyItems().get(0).getThreshold());
        Assertions.assertEquals(82, dtpProperties.getRocketMqTp().get(1).getNotifyItems().get(0).getThreshold());
    }

}
