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

package org.dromara.dynamictp.test.common.entity;

import org.dromara.dynamictp.common.entity.ServiceInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * ServiceInstanceTest related.
 */
class ServiceInstanceTest {

    @Test
    void testAllArgsConstructorSetsFields() {
        ServiceInstance instance = new ServiceInstance("127.0.0.1", 8080, "dynamic-tp", "dev");

        Assertions.assertEquals("127.0.0.1", instance.getIp());
        Assertions.assertEquals(8080, instance.getPort());
        Assertions.assertEquals("dynamic-tp", instance.getServiceName());
        Assertions.assertEquals("dev", instance.getEnv());
    }

    @Test
    void testServiceInstanceCanBeMutatedAndCompared() {
        ServiceInstance first = new ServiceInstance("127.0.0.1", 8080, "dynamic-tp", "dev");
        first.setIp("192.168.0.1");
        first.setPort(9090);
        first.setServiceName("demo");
        first.setEnv("prod");

        ServiceInstance second = new ServiceInstance("192.168.0.1", 9090, "demo", "prod");

        Assertions.assertEquals(second, first);
        Assertions.assertEquals(second.hashCode(), first.hashCode());
        Assertions.assertTrue(first.toString().contains("serviceName=demo"));
    }
}
