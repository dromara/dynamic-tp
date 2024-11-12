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

package org.dromara.dynamictp.test.configcenter.cloud;

import com.google.common.collect.Maps;
import org.dromara.dynamictp.test.configcenter.DtpBaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * CloudRefresherTest related
 *
 * @author yanhom
 * @since 1.1.7
 */
class CloudRefresherTest extends DtpBaseTest {

    @Test
    void testCloudRefresh() {
        int corePoolSize = context.getBean("dtpExecutor1", ThreadPoolExecutor.class).getCorePoolSize();
        System.out.println(corePoolSize);
        Assertions.assertEquals(6, corePoolSize);
        mockEnvironmentChange();
        corePoolSize = context.getBean("dtpExecutor1", ThreadPoolExecutor.class).getCorePoolSize();
        System.out.println(corePoolSize);
        Assertions.assertEquals(10, corePoolSize);
    }

    private void mockEnvironmentChange() {
        MutablePropertySources propertySources = this.environment.getPropertySources();
        Map<String, Object> tmpMap = Maps.newHashMap();
        tmpMap.put("dynamictp.executors[0].threadPoolName", "dtpExecutor1");
        tmpMap.put("dynamictp.executors[0].corePoolSize", 10);
        tmpMap.put("dynamictp.executors[0].maximumPoolSize", 20);

        propertySources.addFirst(new MapPropertySource("DtpCloudRefreshTestPropertySource", tmpMap));
        Set<String> keys = Collections.singleton("dynamictp.executors[0].corePoolSize");
        EnvironmentChangeEvent event = new EnvironmentChangeEvent(keys);
        this.publisher.publishEvent(event);
    }
}
