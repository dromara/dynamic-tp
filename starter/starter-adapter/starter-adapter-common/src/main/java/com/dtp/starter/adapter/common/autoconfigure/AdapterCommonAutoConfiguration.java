package com.dtp.starter.adapter.common.autoconfigure;

import com.dtp.adapter.common.DtpAdapterListener;
import com.dtp.core.spring.DtpBaseBeanConfiguration;
import com.dtp.core.spring.PropertiesBinder;
import com.dtp.starter.adapter.common.autoconfigure.monitor.DtpEndpoint;
import com.dtp.starter.adapter.common.autoconfigure.spring.SpringBootBinder;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * AdapterCommonAutoConfiguration related
 *
 * @author yanhom
 * @since 1.0.9
 **/
@Configuration
@DependsOn("dtpProperties")
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
public class AdapterCommonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PropertiesBinder propertiesBinder() {
        return new SpringBootBinder();
    }
    
    @Bean
    @ConditionalOnAvailableEndpoint
    public DtpEndpoint dtpEndpoint() {
        return new DtpEndpoint();
    }
    
    @Bean
    @DependsOn({"dtpApplicationContextHolder"})
    @ConditionalOnMissingBean
    public DtpAdapterListener dtpAdapterListener() {
        return new DtpAdapterListener();
    }
}
