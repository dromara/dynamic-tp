package org.dromara.dynamictp.starter.cloud.nacos.autoconfigure;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import org.dromara.dynamictp.starter.cloud.nacos.refresher.CloudNacosRefresher;
import org.dromara.dynamictp.core.spring.DtpBaseBeanConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DtpAutoConfiguration for spring cloud nacos application.
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Configuration
@ConditionalOnClass(NacosConfigProperties.class)
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
public class DtpCloudNacosAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean()
    @ConditionalOnClass(NacosConfigManager.class)
    public CloudNacosRefresher cloudNacosRefresher() {
        return new CloudNacosRefresher();
    }
}
