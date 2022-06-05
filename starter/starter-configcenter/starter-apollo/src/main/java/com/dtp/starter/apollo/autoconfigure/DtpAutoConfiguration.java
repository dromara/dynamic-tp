package com.dtp.starter.apollo.autoconfigure;

import com.dtp.common.constant.DynamicTpConst;
import com.dtp.starter.apollo.refresh.ApolloRefresher;
import com.dtp.starter.common.autoconfigure.BaseBeanAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.ctrip.framework.apollo.spring.config.PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED;

/**
 * DtpAutoConfiguration for apollo config center.
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Configuration
@ConditionalOnClass(com.ctrip.framework.apollo.ConfigService.class)
@ConditionalOnProperty(value = {APOLLO_BOOTSTRAP_ENABLED, DynamicTpConst.DTP_ENABLED_PROP},
        havingValue = "true", matchIfMissing = true)
@ImportAutoConfiguration({BaseBeanAutoConfiguration.class})
public class DtpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApolloRefresher apolloRefresher() {
        return new ApolloRefresher();
    }
}