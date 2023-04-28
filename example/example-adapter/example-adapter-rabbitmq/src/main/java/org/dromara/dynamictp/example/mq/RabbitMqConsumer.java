package org.dromara.dynamictp.example.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author fabian4
 */
@Slf4j
@Component
@RabbitListener(queues = "testQueue")
public class RabbitMqConsumer {

    @RabbitHandler
    public void process(String text) {
        log.info("Receiver  : " + text);
    }
}
