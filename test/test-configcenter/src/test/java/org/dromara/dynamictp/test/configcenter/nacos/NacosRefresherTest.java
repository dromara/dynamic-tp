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

package org.dromara.dynamictp.test.configcenter.nacos;

import com.alibaba.nacos.spring.context.event.config.NacosConfigReceivedEvent;
import org.dromara.dynamictp.test.configcenter.DtpBaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.Mockito.mock;

/**
 * NacosRefresherTest related
 *
 * @author yanhom
 * @since 1.1.7
 */
class NacosRefresherTest extends DtpBaseTest {

    @Test
    void testRefresh() throws InterruptedException {
        int corePoolSize = context.getBean("dtpExecutor1", ThreadPoolExecutor.class).getCorePoolSize();
        System.out.println("nacos refresher, corePoolSize before refresh: " + corePoolSize);
        Assertions.assertEquals(6, corePoolSize);
        mockConfigChange();
        Thread.sleep(2000L);
        corePoolSize = context.getBean("dtpExecutor1", ThreadPoolExecutor.class).getCorePoolSize();
        System.out.println("nacos refresher, corePoolSize after refresh: " + corePoolSize);
        Assertions.assertEquals(10, corePoolSize);
    }

    private void mockConfigChange() {
        String dataId = "dynamic-tp-demo-dtp-dev.yml";
        String content =
                "dynamictp:\n" +
                "      enabled: true\n" +
                "      executors:\n" +
                "        - threadPoolName: dtpExecutor1\n" +
                "          threadPoolAliasName: test\n" +
                "          executorType: common\n" +
                "          corePoolSize: 10\n" +
                "          maximumPoolSize: 20\n";

        MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.replace(dataId, createYamlPropertySource(dataId, content));
        publisher.publishEvent(mock(NacosConfigReceivedEvent.class));
    }

    private PropertiesPropertySource createYamlPropertySource(String dataId, String content) {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)));
        Properties properties = factory.getObject();
        return new PropertiesPropertySource(dataId, properties);
    }
}
