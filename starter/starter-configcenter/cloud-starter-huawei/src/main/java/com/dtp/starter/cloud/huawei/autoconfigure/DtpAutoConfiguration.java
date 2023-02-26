package com.dtp.starter.cloud.huawei.autoconfigure;

import com.dtp.common.constant.DynamicTpConst;
import com.dtp.starter.cloud.huawei.refresh.CloudHuaweiRefresher;
import com.dtp.starter.common.autoconfigure.BaseBeanAutoConfiguration;
import com.huaweicloud.common.configration.bootstrap.ConfigBootstrapProperties;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
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
@ConditionalOnProperty(value = DynamicTpConst.DTP_ENABLED_PROP, matchIfMissing = true, havingValue = "true")
@ImportAutoConfiguration({BaseBeanAutoConfiguration.class})
public class DtpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean()
    @ConditionalOnProperty(value = "spring.cloud.servicecomb.config.enabled", matchIfMissing = true)
    public CloudHuaweiRefresher cloudHuaweiRefresher() {
        return new CloudHuaweiRefresher();
    }
}
