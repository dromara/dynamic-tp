package com.dtp.starter.nacos.autoconfigure;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.dtp.common.constant.DynamicTpConst;
import com.dtp.starter.common.config.BaseBeanConfiguration;
import com.dtp.starter.nacos.refresh.CloudNacosRefresher;
import com.dtp.starter.nacos.refresh.NacosRefresher;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DtpAutoConfiguration related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Configuration
@ImportAutoConfiguration({BaseBeanConfiguration.class})
@ConditionalOnProperty(value = DynamicTpConst.DTP_ENABLED_PROP,
        matchIfMissing = true, havingValue = "true")
public class DtpAutoConfiguration {

    @Configuration
    @ConditionalOnClass(NacosConfigProperties.class)
    protected static class SpringCloudNacosConfiguration {

        @Bean
        @ConditionalOnMissingBean()
        public CloudNacosRefresher cloudNacosRefresher() {
            return new CloudNacosRefresher();
        }
    }

    @Configuration
    @ConditionalOnClass(value = com.alibaba.nacos.api.config.ConfigService.class)
    @ConditionalOnMissingClass(value = {"com.alibaba.cloud.nacos.NacosConfigProperties"})
    protected static class NacosConfiguration {

        @Bean
        @ConditionalOnMissingBean()
        public NacosRefresher nacosRefresher() {
            return new NacosRefresher();
        }
    }
}