package org.dromara.dynamictp.starter.adapter.rabbitmq.autoconfigure;

import org.dromara.dynamictp.adapter.rabbitmq.RabbitMqDtpAdapter;
import org.dromara.dynamictp.core.spring.DtpBaseBeanConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
@SuppressWarnings("all")
public class RabbitMqTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RabbitMqDtpAdapter rabbitMqDtpAdapter() {
        return new RabbitMqDtpAdapter();
    }
}
