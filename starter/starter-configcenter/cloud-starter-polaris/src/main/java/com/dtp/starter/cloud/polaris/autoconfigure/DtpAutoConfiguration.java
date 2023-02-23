package com.dtp.starter.cloud.polaris.autoconfigure;

import com.dtp.common.constant.DynamicTpConst;
import com.dtp.starter.cloud.polaris.refresh.CloudPolarisRefresher;
import com.dtp.starter.common.autoconfigure.BaseBeanAutoConfiguration;
import com.tencent.cloud.polaris.config.config.PolarisConfigProperties;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DtpAutoConfiguration for spring cloud polaris application.
 *
 * @author fabian4
 * @since 1.0.0
 **/
@Configuration
@ConditionalOnClass(PolarisConfigProperties.class)
@ConditionalOnProperty(value = DynamicTpConst.DTP_ENABLED_PROP, matchIfMissing = true, havingValue = "true")
@ImportAutoConfiguration({BaseBeanAutoConfiguration.class})
public class DtpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean()
    @ConditionalOnProperty(value = "spring.cloud.polaris.config.enabled", matchIfMissing = true)
    public CloudPolarisRefresher cloudPolarisRefresher() {
        return new CloudPolarisRefresher();
    }
}
