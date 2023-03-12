package com.dtp.test.common.util;

import com.dtp.common.entity.ServiceInstance;
import com.dtp.common.util.ReflectionUtil;
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
