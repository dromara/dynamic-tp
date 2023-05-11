package org.dromara.dynamictp.starter.common;

import org.dromara.dynamictp.core.spring.DtpBaseBeanConfiguration;
import org.dromara.dynamictp.starter.common.monitor.DtpEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DtpBootBeanConfiguration related
 *
 * @author dragon-zhang
 * @since 1.1.4
 */
@Configuration
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
public class DtpBootBeanConfiguration {
    
    @Bean
    @ConditionalOnAvailableEndpoint
    public DtpEndpoint dtpEndpoint() {
        return new DtpEndpoint();
    }
    
}
