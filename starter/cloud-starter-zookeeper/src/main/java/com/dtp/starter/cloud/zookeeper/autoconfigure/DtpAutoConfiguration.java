package com.dtp.starter.cloud.zookeeper.autoconfigure;

import com.dtp.common.constant.DynamicTpConst;
import com.dtp.starter.cloud.zookeeper.refresh.CloudZookeeperRefresher;
import com.dtp.starter.common.autoconfigure.BaseBeanAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.zookeeper.config.ZookeeperConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Redick01
 */
@Configuration
@ConditionalOnClass(ZookeeperConfigProperties.class)
@ConditionalOnProperty(value = DynamicTpConst.DTP_ENABLED_PROP, matchIfMissing = true, havingValue = "true")
@ImportAutoConfiguration({BaseBeanAutoConfiguration.class})
public class DtpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean()
    @ConditionalOnProperty(value = "spring.cloud.zookeeper.config.enabled", matchIfMissing = true)
    public CloudZookeeperRefresher cloudZookeeperRefresher() {
        return new CloudZookeeperRefresher();
    }
}
