package org.dromara.dynamictp.starter.adapter.hystrix.autoconfigure;

import org.dromara.dynamictp.adapter.hystrix.HystrixDtpAdapter;
import org.dromara.dynamictp.core.spring.DtpBaseBeanConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HystrixTpAutoConfiguration related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Configuration
@ConditionalOnClass(name = "com.netflix.hystrix.Hystrix")
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
public class HystrixTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HystrixDtpAdapter hystrixDtpHandler() {
        return new HystrixDtpAdapter();
    }
}
