package com.dtp.core.spring;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.properties.DtpProperties;
import com.dtp.core.DtpRegistry;
import com.dtp.core.monitor.DtpEndpoint;
import com.dtp.core.monitor.DtpMonitor;
import com.dtp.core.support.DtpBannerPrinter;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * BaseBeanAutoConfiguration related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DtpProperties.class)
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
    @DependsOn({"dtpApplicationContextHolder"})
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