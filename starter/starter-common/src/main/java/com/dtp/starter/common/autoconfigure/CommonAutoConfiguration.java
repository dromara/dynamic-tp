package com.dtp.starter.common.autoconfigure;

import com.dtp.core.spring.DtpBaseBeanConfiguration;
import com.dtp.core.spring.PropertiesBinder;
import com.dtp.starter.common.autoconfigure.monitor.DtpEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ServiceLoader;

/**
 * CommonAutoConfiguration related
 *
 * @author dragon-zhang
 * @since 1.1.3
 */
@Configuration
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
public class CommonAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public PropertiesBinder propertiesBinder() {
        ServiceLoader<PropertiesBinder> loader = ServiceLoader.load(PropertiesBinder.class);
        return loader.iterator().next();
    }
    
    @Bean
    @ConditionalOnAvailableEndpoint
    public DtpEndpoint dtpEndpoint() {
        return new DtpEndpoint();
    }
}
