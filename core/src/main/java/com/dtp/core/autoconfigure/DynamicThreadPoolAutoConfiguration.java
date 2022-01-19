package com.dtp.core.autoconfigure;

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
 * @author : wh
 * @date : 2022/1/19 18:11
 * @description:
 */
@Configuration
@EnableConfigurationProperties(value = {DtpProperties.class})
@ConditionalOnProperty(name = DynamicTpConst.DTP_ENABLED_PROP, matchIfMissing = true, havingValue = "true")
public class DynamicThreadPoolAutoConfiguration {

    private final DtpProperties properties;

    public DynamicThreadPoolAutoConfiguration(DtpProperties properties) {
        this.properties = properties;
    }

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
