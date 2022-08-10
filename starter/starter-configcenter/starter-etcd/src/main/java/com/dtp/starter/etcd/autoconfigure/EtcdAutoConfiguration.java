package com.dtp.starter.etcd.autoconfigure;

import com.dtp.starter.etcd.refresh.EtcdRefresher;
import io.etcd.jetcd.Client;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Redick01
 */
@Configuration
@ConditionalOnClass(value = Client.class)
public class EtcdAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EtcdRefresher etcdRefresher() {
        return new EtcdRefresher();
    }
}
