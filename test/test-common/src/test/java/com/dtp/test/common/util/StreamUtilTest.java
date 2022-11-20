package com.dtp.test.common.util;

import com.dtp.common.dto.Instance;
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
        List<Instance> instances = Lists.newArrayList();
        Instance instance = new Instance("172.12.13.1", 9000, "order-service", "prod");
        Instance instance2 = new Instance("172.12.13.2", 9000, "order-service", "prod");
        instances.add(instance);
        instances.add(instance2);

        Map<String, Instance> instanceMap = StreamUtil.toMap(instances, Instance::getServiceName);
        Assertions.assertEquals(instanceMap.get("order-service"), instance2);
    }
}
