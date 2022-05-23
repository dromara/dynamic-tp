package com.dtp.starter.common.autoconfigure.dubbo;

import com.dtp.adapter.dubbo.alibaba.AlibabaDubboEventService;
import com.dtp.adapter.dubbo.alibaba.handler.AlibabaDubboDtpHandler;
import com.dtp.starter.common.autoconfigure.BaseBeanAutoConfiguration;
import com.dtp.starter.common.autoconfigure.dubbo.condition.ConditionOnAlibabaDubboApp;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AlibabaDubboTpAutoConfiguration related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Configuration
@ConditionOnAlibabaDubboApp
@AutoConfigureAfter({BaseBeanAutoConfiguration.class})
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
