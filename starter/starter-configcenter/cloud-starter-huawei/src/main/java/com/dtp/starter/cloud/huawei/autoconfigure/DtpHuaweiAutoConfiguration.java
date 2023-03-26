package com.dtp.starter.cloud.huawei.autoconfigure;

import com.dtp.core.spring.DtpBaseBeanConfiguration;
import com.dtp.starter.cloud.huawei.refresh.CloudHuaweiRefresher;
import com.huaweicloud.common.configration.bootstrap.ConfigBootstrapProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author windsearcher
 */
@Configuration
@ConditionalOnClass(ConfigBootstrapProperties.class)
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
public class DtpHuaweiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean()
    @ConditionalOnProperty(value = "spring.cloud.servicecomb.config.enabled", matchIfMissing = true)
    public CloudHuaweiRefresher cloudHuaweiRefresher() {
        return new CloudHuaweiRefresher();
    }
}
