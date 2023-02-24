package com.dtp.example.mq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author fabian4
 */
@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue Queue() {
        return new Queue("testQueue");
    }
}
