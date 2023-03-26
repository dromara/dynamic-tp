package com.dtp.starter.cloud.polaris.autoconfigure;

import com.dtp.starter.cloud.polaris.refresh.CloudPolarisRefresher;
import com.dtp.core.spring.DtpBaseBeanConfiguration;
import com.tencent.cloud.polaris.config.config.PolarisConfigProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
public class DtpPolarisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean()
    @ConditionalOnProperty(value = "spring.cloud.polaris.config.enabled", matchIfMissing = true)
    public CloudPolarisRefresher cloudPolarisRefresher() {
        return new CloudPolarisRefresher();
    }
}
