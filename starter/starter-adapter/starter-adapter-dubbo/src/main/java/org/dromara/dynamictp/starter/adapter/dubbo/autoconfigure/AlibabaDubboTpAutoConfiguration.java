package org.dromara.dynamictp.starter.adapter.dubbo.autoconfigure;

import org.dromara.dynamictp.adapter.dubbo.alibaba.AlibabaDubboDtpAdapter;
import org.dromara.dynamictp.starter.adapter.dubbo.autoconfigure.condition.ConditionOnAlibabaDubboApp;
import org.dromara.dynamictp.core.spring.DtpBaseBeanConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AlibabaDubboTpAutoConfiguration related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Configuration
@ConditionOnAlibabaDubboApp
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
@SuppressWarnings("all")
public class AlibabaDubboTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AlibabaDubboDtpAdapter alibabaDubboDtpHandler() {
        return new AlibabaDubboDtpAdapter();
    }
}
