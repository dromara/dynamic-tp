package com.dtp.test.common.util;

import com.dtp.common.dto.Instance;
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
        Instance instance = new Instance(ip, 9000, "order-service", "prod");
        Object fieldVal = ReflectionUtil.getFieldValue(Instance.class, "ip", instance);
        Assertions.assertEquals(ip, fieldVal);
    }

    @Test
    void testSetFieldValue() throws IllegalAccessException {
        String ip = "172.12.13.1";
        String newIp = "172.12.13.2";
        Instance instance = new Instance(ip, 9000, "order-service", "prod");
        ReflectionUtil.setFieldValue(Instance.class, "ip", instance, newIp);
        Assertions.assertEquals(newIp, instance.getIp());
    }

    @Test
    void testGetField() {
        Field ipField = ReflectionUtil.getField(Instance.class, "ip");
        Field ippField = ReflectionUtil.getField(Instance.class, "ipp");
        Assertions.assertNotNull(ipField);
        Assertions.assertNull(ippField);
        Assertions.assertEquals("ip", ipField.getName());
    }
}
