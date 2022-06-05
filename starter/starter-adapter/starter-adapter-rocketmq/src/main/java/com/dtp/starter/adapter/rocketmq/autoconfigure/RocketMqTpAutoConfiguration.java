package com.dtp.starter.adapter.rocketmq.autoconfigure;

import com.dtp.adapter.rocketmq.RocketMqEventService;
import com.dtp.adapter.rocketmq.handler.RocketMqDtpHandler;
import com.dtp.starter.common.autoconfigure.BaseBeanAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
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
@AutoConfigureAfter({BaseBeanAutoConfiguration.class})
@SuppressWarnings("all")
public class RocketMqTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RocketMqDtpHandler rocketMqDtpHandler() {
        return new RocketMqDtpHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public RocketMqEventService rocketMqEventService() {
        return new RocketMqEventService();
    }
}
