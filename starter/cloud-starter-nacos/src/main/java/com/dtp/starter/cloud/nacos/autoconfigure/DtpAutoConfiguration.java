package com.dtp.starter.cloud.nacos.autoconfigure;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.dtp.common.constant.DynamicTpConst;
import com.dtp.core.config.BaseBeanConfiguration;
import com.dtp.starter.cloud.nacos.refresh.CloudNacosRefresher;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DtpAutoConfiguration for spring cloud nacos config center.
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Configuration
@ConditionalOnClass(NacosConfigProperties.class)
@ImportAutoConfiguration({BaseBeanConfiguration.class})
@ConditionalOnProperty(value = DynamicTpConst.DTP_ENABLED_PROP, matchIfMissing = true, havingValue = "true")
public class DtpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean()
    public CloudNacosRefresher cloudNacosRefresher() {
        return new CloudNacosRefresher();
    }
}