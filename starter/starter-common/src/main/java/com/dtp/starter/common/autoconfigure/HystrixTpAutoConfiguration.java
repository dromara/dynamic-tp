package com.dtp.starter.common.autoconfigure;

import com.dtp.adapter.hystrix.HystrixEventService;
import com.dtp.adapter.hystrix.handler.HystrixDtpHandler;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HystrixTpAutoConfiguration related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Configuration
@ConditionalOnClass(name = "com.netflix.hystrix.Hystrix")
@AutoConfigureAfter({BaseBeanAutoConfiguration.class})
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
