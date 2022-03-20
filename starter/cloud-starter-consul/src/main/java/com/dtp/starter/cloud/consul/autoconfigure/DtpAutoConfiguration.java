package com.dtp.starter.cloud.consul.autoconfigure;

import com.dtp.common.constant.DynamicTpConst;
import com.dtp.starter.cloud.consul.refresh.CloudConsulRefresher;
import com.dtp.starter.common.autoconfigure.BaseBeanAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.consul.config.ConsulConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Redick01
 */
@Configuration
@ConditionalOnClass(ConsulConfigProperties.class)
@ConditionalOnProperty(value = DynamicTpConst.DTP_ENABLED_PROP, matchIfMissing = true, havingValue = "true")
@ImportAutoConfiguration({BaseBeanAutoConfiguration.class})
public class DtpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean()
    public CloudConsulRefresher cloudZookeeperRefresher() {
        return new CloudConsulRefresher();
    }
}
