package com.dtp.starter.etcd.autoconfigure;

import com.dtp.starter.common.autoconfigure.BaseBeanAutoConfiguration;
import com.dtp.starter.etcd.refresh.EtcdRefresher;
import io.etcd.jetcd.Client;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Redick01
 */
@Configuration
@ConditionalOnClass(value = Client.class)
@ConditionalOnBean({BaseBeanAutoConfiguration.class})
@AutoConfigureAfter({BaseBeanAutoConfiguration.class})
public class DtpEtcdAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EtcdRefresher etcdRefresher() {
        return new EtcdRefresher();
    }
}
