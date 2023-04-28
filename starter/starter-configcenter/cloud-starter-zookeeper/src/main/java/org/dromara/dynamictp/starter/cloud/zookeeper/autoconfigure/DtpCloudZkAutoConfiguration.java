package org.dromara.dynamictp.starter.cloud.zookeeper.autoconfigure;

import com.dtp.common.properties.DtpProperties;
import org.dromara.dynamictp.starter.cloud.zookeeper.refresh.CloudZookeeperRefresher;
import org.dromara.dynamictp.core.spring.DtpBaseBeanConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
public class DtpCloudZkAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean()
    @ConditionalOnProperty(value = "spring.cloud.zookeeper.config.enabled", matchIfMissing = true)
    public CloudZookeeperRefresher cloudZookeeperRefresher(DtpProperties dtpProperties) {
        return new CloudZookeeperRefresher(dtpProperties);
    }
}
