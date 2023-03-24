package com.dtp.starter.adapter.rocketmq.autoconfigure;

import com.dtp.adapter.rocketmq.RocketMqDtpAdapter;
import com.dtp.core.spring.DtpBaseBeanConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMqTpAutoConfiguration related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Configuration
@ConditionalOnProperty(prefix = "rocketmq", value = {"name-server"})
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
@SuppressWarnings("all")
public class RocketMqTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RocketMqDtpAdapter rocketMqDtpHandler() {
        return new RocketMqDtpAdapter();
    }
}
