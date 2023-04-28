package org.dromara.dynamictp.starter.zookeeper.autoconfigure;

import com.dtp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.spring.DtpBaseBeanConfiguration;
import org.dromara.dynamictp.starter.zookeeper.refresh.ZookeeperRefresher;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Redick01
 */
@Configuration
@ConditionalOnClass(value = org.apache.curator.framework.CuratorFramework.class)
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
public class DtpZkAutoConfiguration {

    @Bean
    public ZookeeperRefresher zookeeperRefresher(DtpProperties dtpProperties) {
        return new ZookeeperRefresher(dtpProperties);
    }
}
