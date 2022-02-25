package com.dtp.starter.cloud.nacos.autoconfigure;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.dtp.common.constant.DynamicTpConst;
import com.dtp.starter.cloud.nacos.refresh.CloudNacosRefresher;
import com.dtp.starter.common.autoconfigure.BaseBeanAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DtpAutoConfiguration for spring cloud nacos application.
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Configuration
@ConditionalOnClass(NacosConfigProperties.class)
@ConditionalOnProperty(value = DynamicTpConst.DTP_ENABLED_PROP, matchIfMissing = true, havingValue = "true")
@ImportAutoConfiguration({BaseBeanAutoConfiguration.class})
public class DtpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean()
    @ConditionalOnClass(NacosConfigManager.class)
    public CloudNacosRefresher cloudNacosRefresher() {
        return new CloudNacosRefresher();
    }
}