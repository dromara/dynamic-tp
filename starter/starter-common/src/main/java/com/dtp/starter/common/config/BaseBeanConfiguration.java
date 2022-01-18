package com.dtp.starter.common.config;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.DtpBannerPrinter;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.constant.DynamicTpConst;
import com.dtp.core.DtpRegistry;
import com.dtp.core.monitor.DtpMonitor;
import com.dtp.core.monitor.endpoint.DtpEndpoint;
import com.dtp.core.support.DtpPostProcessor;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * BaseBeanConfiguration related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Configuration
@ImportAutoConfiguration({DtpProperties.class})
@ConditionalOnProperty(prefix = DynamicTpConst.MAIN_PROPERTIES_PREFIX,
        value = "enabled", matchIfMissing = true, havingValue = "true")
public class BaseBeanConfiguration {

    @Resource
    private DtpProperties properties;

    @Bean
    public ApplicationContextHolder dtpApplicationContextHolder() {
        return new ApplicationContextHolder();
    }

    @Bean
    @ConditionalOnMissingBean
    public DtpBannerPrinter dtpBannerPrinter() {
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