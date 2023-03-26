package com.dtp.starter.apollo.autoconfigure;

import com.dtp.starter.apollo.refresh.ApolloRefresher;
import com.dtp.core.spring.DtpBaseBeanConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.ctrip.framework.apollo.spring.config.PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED;

/**
 * DtpAutoConfiguration for apollo config center.
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Configuration
@ConditionalOnClass(com.ctrip.framework.apollo.ConfigService.class)
@ConditionalOnProperty(value = {APOLLO_BOOTSTRAP_ENABLED}, havingValue = "true", matchIfMissing = true)
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
public class DtpApolloAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApolloRefresher apolloRefresher() {
        return new ApolloRefresher();
    }
}
