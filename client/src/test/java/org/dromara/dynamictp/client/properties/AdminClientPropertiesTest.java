package org.dromara.dynamictp.client.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.env.MockEnvironment;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AdminClientPropertiesTest {

    @Test
    void shouldBindFromDynamictpPrefix() {
        Map<String, Object> map = new HashMap<>();
        map.put("dynamictp.adminEnabled", "true");
        map.put("dynamictp.adminNodes", "127.0.0.1:6691");
        map.put("dynamictp.loadBalanceStrategy", "random");
        map.put("dynamictp.clientName", "client-a");
        map.put("dynamictp.serviceName", "svc-a");

        MockEnvironment env = new MockEnvironment();
        env.getPropertySources().addFirst(new MapPropertySource("test", map));
        ConfigurationPropertySources.attach(env);

        AdminClientProperties props = Binder.get(env)
                .bind("dynamictp", AdminClientProperties.class)
                .orElseThrow(IllegalStateException::new);

        assertTrue(props.isAdminEnabled());
        assertEquals("127.0.0.1:6691", props.getAdminNodes());
        assertEquals("random", props.getLoadBalanceStrategy());
        assertEquals("client-a", props.getClientName());
        assertEquals("svc-a", props.getServiceName());
    }

    @Test
    void shouldHaveDefaults() {
        AdminClientProperties props = new AdminClientProperties();
        assertFalse(props.isAdminEnabled());
        assertEquals("roundRobin", props.getLoadBalanceStrategy());
    }
}
