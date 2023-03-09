package com.dtp.starter.nacos.autoconfigure;

import com.dtp.starter.common.autoconfigure.BaseBeanAutoConfiguration;
import com.dtp.starter.nacos.refresh.NacosRefresher;
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
@ConditionalOnBean({BaseBeanAutoConfiguration.class})
@AutoConfigureAfter({BaseBeanAutoConfiguration.class})
public class DtpNacosAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean()
    public NacosRefresher nacosRefresher() {
        return new NacosRefresher();
    }

}
