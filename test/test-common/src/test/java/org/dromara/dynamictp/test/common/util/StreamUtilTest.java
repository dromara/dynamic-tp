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

package org.dromara.dynamictp.test.common.util;

import org.dromara.dynamictp.common.entity.ServiceInstance;
import org.dromara.dynamictp.common.util.StreamUtil;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * StreamUtil related
 *
 * @author yanhom
 */
class StreamUtilTest {

    @Test
    void testToMap() {
        List<ServiceInstance> serviceInstances = Lists.newArrayList();
        ServiceInstance serviceInstance = new ServiceInstance("172.12.13.1", 9000, "order-service", "prod");
        ServiceInstance serviceInstance2 = new ServiceInstance("172.12.13.2", 9000, "order-service", "prod");
        serviceInstances.add(serviceInstance);
        serviceInstances.add(serviceInstance2);

        Map<String, ServiceInstance> instanceMap = StreamUtil.toMap(serviceInstances, ServiceInstance::getServiceName);
        Assertions.assertEquals(instanceMap.get("order-service"), serviceInstance2);
    }
}
