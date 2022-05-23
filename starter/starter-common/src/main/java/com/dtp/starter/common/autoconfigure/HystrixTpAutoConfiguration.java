package com.dtp.starter.common.autoconfigure;

import com.dtp.adapter.hystrix.handler.HystrixDtpHandler;
import com.dtp.adapter.hystrix.HystrixEventService;
import com.dtp.common.config.DtpProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.dtp.common.constant.DynamicTpConst.DTP_ENABLED_PROP;

/**
 * HystrixTpAutoConfiguration related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Configuration
@EnableConfigurationProperties(DtpProperties.class)
@ConditionalOnProperty(name= DTP_ENABLED_PROP, matchIfMissing = true, havingValue = "true")
@ConditionalOnClass(name = "com.netflix.hystrix.Hystrix")
public class HystrixTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HystrixDtpHandler hystrixDtpHandler() {
        return new HystrixDtpHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public HystrixEventService hystrixEventService() {
        return new HystrixEventService();
    }
}
