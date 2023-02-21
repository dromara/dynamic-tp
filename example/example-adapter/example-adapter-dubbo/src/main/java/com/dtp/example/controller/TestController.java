package com.dtp.example.controller;

import com.dtp.example.dubbo.DubboUserService;
import lombok.extern.slf4j.Slf4j;
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
    private DubboUserService dubboUserService;

    @GetMapping("/dtp-example-adapter/testDubbo")
    public String testDubbo() throws InterruptedException {
        return dubboUserService.getUserName(1001);
    }
}
