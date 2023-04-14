package com.dtp.test.core.spring;

import com.dtp.common.properties.DtpProperties;
import com.dtp.core.spring.PropertiesBinder;
import com.dtp.core.spring.YamlPropertySourceFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.AbstractEnvironment;

import java.util.List;
import java.util.Map;

/**
 * PropertiesBinderTest related
 *
 * @author yanhom
 * @since 1.1.0
 */
@PropertySource(value = "classpath:/demo-dtp-dev.yml",
        factory = YamlPropertySourceFactory.class)
@SpringBootTest(classes = PropertiesBinderTest.class)
@EnableAutoConfiguration
class PropertiesBinderTest {

    @Autowired
    private AbstractEnvironment environment;
    
    @Autowired
    private PropertiesBinder propertiesBinder;

    @Test
    void testBindDtpPropertiesWithMap() {
        Map<Object, Object> properties  = Maps.newHashMap();
        properties.put("spring.dynamic.tp.enabled", false);
        properties.put("spring.dynamic.tp.collectorTypes", Lists.newArrayList("LOGGING"));
        properties.put("spring.dynamic.tp.executors[0].threadPoolName", "test_dtp");

        DtpProperties dtpProperties = new DtpProperties();
        propertiesBinder.bindDtpProperties(properties, dtpProperties);
        Assertions.assertEquals(properties.get("spring.dynamic.tp.executors[0].threadPoolName"),
                dtpProperties.getExecutors().get(0).getThreadPoolName());
        Assertions.assertIterableEquals((List<String>) properties.get("spring.dynamic.tp.collectorTypes"),
                dtpProperties.getCollectorTypes());
    }

    @Test
    void testBindDtpPropertiesWithEnvironment() {
        DtpProperties dtpProperties = propertiesBinder.bindDtpProperties(environment, new DtpProperties());
        String threadPoolName = environment.getProperty("spring.dynamic.tp.executors[0].threadPoolName");
        Assertions.assertEquals(threadPoolName, dtpProperties.getExecutors().get(0).getThreadPoolName());
    }

}
