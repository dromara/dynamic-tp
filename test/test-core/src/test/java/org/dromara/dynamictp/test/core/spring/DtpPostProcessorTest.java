package org.dromara.dynamictp.test.core.spring;

import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.spring.EnableDynamicTp;
import org.dromara.dynamictp.core.spring.YamlPropertySourceFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.Executor;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
@EnableDynamicTp
@EnableAutoConfiguration
@PropertySource(value = "classpath:/postprocessor-dtp-dev.yml", factory = YamlPropertySourceFactory.class)
@ComponentScan(basePackages = "com.dtp.test.core.spring")
public class DtpPostProcessorTest {

    private static ConfigurableApplicationContext context;

    @BeforeAll
    public static void setUp() {
        context = SpringApplication.run(DtpPostProcessorTest.class);
    }

    @Test
    void test() {
        Executor executor = DtpRegistry.getExecutor("asyncExecutor");
        Assertions.assertNotNull(executor);
    }

}
