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

import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.spring.context.event.config.NacosConfigReceivedEvent;
import com.alibaba.nacos.spring.core.env.NacosPropertySource;
import org.dromara.dynamictp.test.configcenter.DtpBaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MutablePropertySources;

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

        // 打印所有bean名称
        String[] beanNames = context.getBeanDefinitionNames();
        System.out.println("所有bean名称：");
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }

        assert context.containsBean("dtpExecutor1") : "dtpExecutor1 bean not found!";

        int corePoolSize = context.getBean("dtpExecutor1", ThreadPoolExecutor.class).getCorePoolSize();
        System.out.println(corePoolSize);
        Assertions.assertEquals(6, corePoolSize);
        mockConfigChange();
        Thread.sleep(2000L);
        corePoolSize = context.getBean("dtpExecutor1", ThreadPoolExecutor.class).getCorePoolSize();
        System.out.println(corePoolSize);
        Assertions.assertEquals(10, corePoolSize);
    }

    private void mockConfigChange() {
        String dataId = "dynamic-tp-demo-dtp-dev.yml";
        String groupId = "DEFAULT_GROUP";
        String type = ConfigType.YAML.getType();
        String content =
                "dynamictp:\n" +
                "      enabled: true                               # 是否启用 dynamictp，默认true\n" +
                "      executors:                                  # 动态线程池配置，都有默认值，采用默认值的可以不配置该项，减少配置量\n" +
                "        - threadPoolName: dtpExecutor1\n" +
                "          threadPoolAliasName: 测试线程池        # 线程池别名\n" +
                "          executorType: common                 # 线程池类型 common、eager、ordered、scheduled，默认 common\n" +
                "          corePoolSize: 10                      # 核心线程数，默认1\n" +
                "          maximumPoolSize: 20                   # 最大线程数，默认cpu核数\n";

        Listener listener = new AbstractListener() {
            @Override
            public void receiveConfigInfo(String config) {
                NacosPropertySource newNacosPropertySource = new NacosPropertySource(dataId, groupId, dataId, config, type);
                MutablePropertySources propertySources = environment.getPropertySources();
                // replace NacosPropertySource
                propertySources.replace(dataId, newNacosPropertySource);
            }
        };
        listener.receiveConfigInfo(content);
        publisher.publishEvent(mock(NacosConfigReceivedEvent.class));
    }
}
