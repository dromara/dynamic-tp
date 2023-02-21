package com.dtp.example.mq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author fabian
 */
@Component
public class RocketMqProducer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    // 发送消息的实例
    public void sendMessage(String msg) {
        rocketMQTemplate.convertAndSend(RocketMqListener.TOPIC, msg);
    }
}

