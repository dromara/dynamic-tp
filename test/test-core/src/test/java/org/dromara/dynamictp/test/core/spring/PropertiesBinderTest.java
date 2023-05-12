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
class PropertiesBinderTest {

    @Autowired
    private AbstractEnvironment environment;

    @Test
    void testBindDtpPropertiesWithMap() {
        Map<Object, Object> properties  = Maps.newHashMap();
        properties.put("spring.dynamic.tp.enabled", false);
        properties.put("spring.dynamic.tp.collectorTypes", Lists.newArrayList("LOGGING"));
        properties.put("spring.dynamic.tp.executors[0].threadPoolName", "test_dtp");

        DtpProperties dtpProperties = DtpProperties.getInstance();
        BinderHelper.bindDtpProperties(properties, dtpProperties);
        Assertions.assertEquals(properties.get("spring.dynamic.tp.executors[0].threadPoolName"),
                dtpProperties.getExecutors().get(0).getThreadPoolName());
        Assertions.assertIterableEquals((List<String>) properties.get("spring.dynamic.tp.collectorTypes"),
                dtpProperties.getCollectorTypes());
    }

    @Test
    void testBindDtpPropertiesWithEnvironment() {
        DtpProperties dtpProperties = DtpProperties.getInstance();
        BinderHelper.bindDtpProperties(environment, dtpProperties);
        String threadPoolName = environment.getProperty("spring.dynamic.tp.executors[0].threadPoolName");
        Assertions.assertEquals(threadPoolName, dtpProperties.getExecutors().get(0).getThreadPoolName());
    }

}
