package com.dtp.example.controller;

import com.dtp.example.mq.RocketMqProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author fabian4
 */
@Slf4j
@RestController
@SuppressWarnings("all")
public class TestController {

    @Autowired
    private RocketMqProducer rocketMqProducer;

    @GetMapping("/dtp-example-adapter/testRocketMq")
    public String testRocketMq() throws InterruptedException {
        rocketMqProducer.sendMessage("hello, Dynamtic-yp");
        return "success";
    }
}
