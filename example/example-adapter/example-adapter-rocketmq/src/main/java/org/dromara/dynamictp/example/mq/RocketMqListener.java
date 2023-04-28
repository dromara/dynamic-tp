package org.dromara.dynamictp.example.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import static org.dromara.dynamictp.example.mq.RocketMqListener.GROUP;
import static org.dromara.dynamictp.example.mq.RocketMqListener.TOPIC;

/**
 * TestRocketMqListener related
 *
 * @author yanhom
 * @since 1.0.9
 */
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = GROUP, topic = TOPIC)
public class RocketMqListener implements RocketMQListener<MessageExt> {

    public static final String GROUP = "group";
    public static final String TOPIC = "topic";

    @Override
    public void onMessage(MessageExt message) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("message: {}", message);
    }
}
