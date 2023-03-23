package com.dtp.starter.adapter.dubbo.autoconfigure;

import com.dtp.adapter.dubbo.alibaba.AlibabaDubboDtpAdapter;
import com.dtp.starter.adapter.dubbo.autoconfigure.condition.ConditionOnAlibabaDubboApp;
import com.dtp.core.spring.BaseBeanAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
@ConditionalOnBean({BaseBeanAutoConfiguration.class})
@SuppressWarnings("all")
public class AlibabaDubboTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AlibabaDubboDtpAdapter alibabaDubboDtpHandler() {
        return new AlibabaDubboDtpAdapter();
    }
}
