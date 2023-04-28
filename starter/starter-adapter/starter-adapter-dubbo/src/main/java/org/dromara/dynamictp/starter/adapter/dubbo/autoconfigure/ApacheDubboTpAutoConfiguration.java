package org.dromara.dynamictp.starter.adapter.dubbo.autoconfigure;

import org.dromara.dynamictp.adapter.dubbo.apache.ApacheDubboDtpAdapter;
import org.dromara.dynamictp.starter.adapter.dubbo.autoconfigure.condition.ConditionOnApacheDubboApp;
import org.dromara.dynamictp.core.spring.DtpBaseBeanConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ApacheDubboTpAutoConfiguration related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Configuration
@ConditionOnApacheDubboApp
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
@SuppressWarnings("all")
public class ApacheDubboTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApacheDubboDtpAdapter apacheDubboDtpHandler() {
        return new ApacheDubboDtpAdapter();
    }
}
