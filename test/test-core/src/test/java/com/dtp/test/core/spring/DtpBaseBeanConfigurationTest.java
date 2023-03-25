package com.dtp.test.core.spring;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.core.spring.DtpBaseBeanConfiguration;
import com.dtp.core.spring.EnableDynamicTp;
import com.dtp.core.spring.YamlPropertySourceFactory;
import com.dtp.core.support.DtpBannerPrinter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;

/**
 * @author <a href = "kamtohung@gmail.com">KamTo Hung</a>
 */
@PropertySource(value = "classpath:/demo-dtp-dev.yml", factory = YamlPropertySourceFactory.class)
@SpringBootTest(classes = DtpBaseBeanConfigurationTest.class)
public class DtpBaseBeanConfigurationTest {

    @SpringBootTest(classes = DtpBaseBeanConfigurationTest.class)
    @EnableDynamicTp
    public static class EnableDtpAonnaction {

        @Autowired
        private ApplicationContext applicationContext;

        @Test
        void test() {
            Assertions.assertNotNull(applicationContext.getBean(DtpBaseBeanConfiguration.class));
            Assertions.assertNotNull(applicationContext.getBean(ApplicationContextHolder.class));
            Assertions.assertNotNull(applicationContext.getBean(DtpBannerPrinter.class));
        }

    }

    @SpringBootTest(classes = DtpBaseBeanConfigurationTest.class)
    public static class DisableDtpAonnaction {

        @Autowired
        private ApplicationContext applicationContext;

        @Test
        void test() {
            Assertions.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(DtpBaseBeanConfiguration.class));
            Assertions.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(ApplicationContextHolder.class));
            Assertions.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(DtpBannerPrinter.class));
        }

    }

}
