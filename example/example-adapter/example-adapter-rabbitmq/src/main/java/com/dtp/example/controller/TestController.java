package com.dtp.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author fabian4
 */
@Slf4j
@RestController
@SuppressWarnings("all")
public class TestController {

    @Resource
    RabbitTemplate rabbitTemplate;  //使用RabbitTemplate,这提供了接收/发送等等方法

    @GetMapping("/dtp-example-adapter/testRabbitMq")
    public String sendDirectMessage() {

        rabbitTemplate.convertAndSend("testQueue", "hello, dynamic-tp");
        return "ok";
    }

}
