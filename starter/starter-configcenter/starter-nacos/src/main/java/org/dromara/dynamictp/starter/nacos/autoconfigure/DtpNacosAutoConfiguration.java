package org.dromara.dynamictp.starter.nacos.autoconfigure;

import org.dromara.dynamictp.core.spring.DtpBaseBeanConfiguration;
import org.dromara.dynamictp.starter.nacos.refresher.NacosRefresher;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DtpAutoConfiguration for not spring cloud nacos application.
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Configuration
@ConditionalOnClass(value = com.alibaba.nacos.api.config.ConfigService.class)
@ConditionalOnMissingClass(value = {"com.alibaba.cloud.nacos.NacosConfigProperties"})
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
public class DtpNacosAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean()
    public NacosRefresher nacosRefresher() {
        return new NacosRefresher();
    }

}
