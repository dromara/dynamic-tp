package com.dtp.starter.adapter.dubbo.autoconfigure;

import com.dtp.adapter.dubbo.apache.ApacheDubboEventService;
import com.dtp.adapter.dubbo.apache.handler.ApacheDubboDtpHandler;
import com.dtp.starter.adapter.dubbo.autoconfigure.condition.ConditionOnApacheDubboApp;
import com.dtp.starter.common.autoconfigure.BaseBeanAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ApacheDubboTpAutoConfiguration related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Configuration
@ConditionOnApacheDubboApp
@AutoConfigureAfter({BaseBeanAutoConfiguration.class})
@SuppressWarnings("all")
public class ApacheDubboTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApacheDubboDtpHandler apacheDubboDtpHandler() {
        return new ApacheDubboDtpHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApacheDubboEventService apacheDubboEventService() {
        return new ApacheDubboEventService();
    }
}
