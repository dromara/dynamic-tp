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
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

/**
 * ReflectionUtilTest related
 *
 * @author yanhom
 */
class ReflectionUtilTest {

    @Test
    void testGetFieldValue() {
        String ip = "172.12.13.1";
        ServiceInstance serviceInstance = new ServiceInstance(ip, 9000, "order-service", "prod");
        Object fieldVal = ReflectionUtil.getFieldValue(ServiceInstance.class, "ip", serviceInstance);
        Assertions.assertEquals(ip, fieldVal);
    }

    @Test
    void testSetFieldValue() throws IllegalAccessException {
        String ip = "172.12.13.1";
        String newIp = "172.12.13.2";
        ServiceInstance serviceInstance = new ServiceInstance(ip, 9000, "order-service", "prod");
        ReflectionUtil.setFieldValue(ServiceInstance.class, "ip", serviceInstance, newIp);
        Assertions.assertEquals(newIp, serviceInstance.getIp());
    }

    @Test
    void testGetField() {
        Field ipField = ReflectionUtil.getField(ServiceInstance.class, "ip");
        Field ippField = ReflectionUtil.getField(ServiceInstance.class, "ipp");
        Assertions.assertNotNull(ipField);
        Assertions.assertNull(ippField);
        Assertions.assertEquals("ip", ipField.getName());
    }
}
