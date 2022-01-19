package com.dtp.starter.common.autoconfigure;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.constant.DynamicTpConst;
import com.dtp.core.DtpRegistry;
import com.dtp.core.monitor.DtpMonitor;
import com.dtp.core.monitor.endpoint.DtpEndpoint;
import com.dtp.core.support.DtpBannerPrinter;
import com.dtp.core.support.DtpPostProcessor;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * BaseBeanAutoConfiguration related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Configuration
@EnableConfigurationProperties(DtpProperties.class)
@ConditionalOnProperty(name = DynamicTpConst.DTP_ENABLED_PROP, matchIfMissing = true, havingValue = "true")
public class BaseBeanAutoConfiguration {

    @Bean
    public ApplicationContextHolder dtpApplicationContextHolder() {
        return new ApplicationContextHolder();
    }

    @Bean
    @ConditionalOnMissingBean
    public DtpBannerPrinter dtpBannerPrinter(DtpProperties properties) {
        return new DtpBannerPrinter(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public DtpPostProcessor dtpPostProcessor() {
        return new DtpPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public DtpRegistry dtpRegistry() {
        return new DtpRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public DtpMonitor dtpMonitor() {
        return new DtpMonitor();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnAvailableEndpoint
    public DtpEndpoint dtpEndpoint() {
        return new DtpEndpoint();
    }
}