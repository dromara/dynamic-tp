package com.dtp.example.adapter.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import static com.dtp.example.adapter.mq.TestRocketMqListener.GROUP;
import static com.dtp.example.adapter.mq.TestRocketMqListener.TOPIC;

/**
 * TestRocketMqListener related
 *
 * @author yanhom
 * @since 1.0.9
 */
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = GROUP, topic = TOPIC)
public class TestRocketMqListener implements RocketMQListener<MessageExt> {

    public static final String GROUP = "test";
    public static final String TOPIC = "test";

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
