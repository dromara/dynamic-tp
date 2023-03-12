package com.dtp.test.common.util;

import com.dtp.common.entity.ServiceInstance;
import com.dtp.common.util.StreamUtil;
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
