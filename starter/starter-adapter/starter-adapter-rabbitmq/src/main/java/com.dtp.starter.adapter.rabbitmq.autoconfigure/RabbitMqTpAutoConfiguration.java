package com.dtp.starter.adapter.rabbitmq.autoconfigure;

import com.dtp.adapter.rabbitmq.RabbitMqDtpAdapter;
import com.dtp.starter.common.autoconfigure.BaseBeanAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMqTpAutoConfiguration related
 *
 * @author fabian
 * @since 1.0.6
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.rabbitmq", value = {"host"})
@AutoConfigureAfter({BaseBeanAutoConfiguration.class})
@SuppressWarnings("all")
public class RabbitMqTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RabbitMqDtpAdapter rabbitMqDtpAdapter() {
        return new RabbitMqDtpAdapter();
    }
}
