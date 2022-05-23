package com.dtp.starter.common.autoconfigure.dubbo;

import com.dtp.adapter.dubbo.apache.ApacheDubboEventService;
import com.dtp.adapter.dubbo.apache.handler.ApacheDubboDtpHandler;
import com.dtp.common.config.DtpProperties;
import com.dtp.starter.common.autoconfigure.dubbo.condition.ConditionOnApacheDubboApp;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.dtp.common.constant.DynamicTpConst.DTP_ENABLED_PROP;

/**
 * ApacheDubboTpAutoConfiguration related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Configuration
@EnableConfigurationProperties(DtpProperties.class)
@ConditionalOnWebApplication
@ConditionalOnProperty(name= DTP_ENABLED_PROP, matchIfMissing = true, havingValue = "true")
@ConditionOnApacheDubboApp
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
