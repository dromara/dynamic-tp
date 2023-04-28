package org.dromara.dynamictp.example.controller;

import org.dromara.dynamictp.example.dubbo.DubboUserService;
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
    private DubboUserService dubboUserService;

    @GetMapping("/dtp-example-adapter/testDubbo")
    public String testDubbo() throws InterruptedException {
        return dubboUserService.getUserName(1001);
    }
}
