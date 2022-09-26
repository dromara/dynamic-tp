package com.dtp.example.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import static com.dtp.example.mq.TestListener.GROUP;
import static com.dtp.example.mq.TestListener.TOPIC;

/**
 * TestListener related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = GROUP, topic = TOPIC)
public class TestListener implements RocketMQListener<MessageExt> {

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
