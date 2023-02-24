package com.dtp.example.mq;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author fabian4
 */
@Component
@RabbitListener(queues = "testQueue")
public class RabbitMqConsumer {

    @RabbitHandler
    public void process(String text) {
        System.out.println("Receiver  : " + text);
    }
}
