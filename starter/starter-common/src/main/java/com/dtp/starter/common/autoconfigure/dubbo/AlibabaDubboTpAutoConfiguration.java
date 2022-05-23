package com.dtp.starter.common.autoconfigure.dubbo;

import com.dtp.adapter.dubbo.alibaba.AlibabaDubboEventService;
import com.dtp.adapter.dubbo.alibaba.handler.AlibabaDubboDtpHandler;
import com.dtp.common.config.DtpProperties;
import com.dtp.starter.common.autoconfigure.dubbo.condition.ConditionOnAlibabaDubboApp;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.dtp.common.constant.DynamicTpConst.DTP_ENABLED_PROP;

/**
 * AlibabaDubboTpAutoConfiguration related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Configuration
@EnableConfigurationProperties(DtpProperties.class)
@ConditionalOnWebApplication
@ConditionalOnProperty(name= DTP_ENABLED_PROP, matchIfMissing = true, havingValue = "true")
@ConditionOnAlibabaDubboApp
@SuppressWarnings("all")
public class AlibabaDubboTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AlibabaDubboDtpHandler alibabaDubboDtpHandler() {
        return new AlibabaDubboDtpHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public AlibabaDubboEventService alibabaDubboEventService() {
        return new AlibabaDubboEventService();
    }
}
