package com.dtp.starter.etcd.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dtp.common.properties.DtpProperties.Etcd;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Redick01
 */
@ExtendWith(MockitoExtension.class)
public class EtcdUtilTest {

    @Test
    public void testGetConfigContent() {
        Etcd etcd = new Etcd();
        etcd.setEndpoints("http://127.0.0.1:2379");
        etcd.setKey("/config/dynamic-tp-etcd-demo");
        etcd.setAuthEnable(false);
        String configType = "properties";
        Map<Object, Object> map = EtcdUtil.getConfigMap(etcd, configType);
        assertTrue(map.size() > 0);
    }
}
