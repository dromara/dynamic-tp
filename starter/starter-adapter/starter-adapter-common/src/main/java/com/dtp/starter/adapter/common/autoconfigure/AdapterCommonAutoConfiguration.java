package com.dtp.starter.adapter.common.autoconfigure;

import com.dtp.adapter.common.DtpAdapterListener;
import com.dtp.common.properties.DtpProperties;
import com.dtp.core.spring.DtpBaseBeanConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties(DtpProperties.class)
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
public class AdapterCommonAutoConfiguration {

    @Bean
    @DependsOn({"dtpApplicationContextHolder"})
    @ConditionalOnMissingBean
    public DtpAdapterListener dtpAdapterListener() {
        return new DtpAdapterListener();
    }
}
