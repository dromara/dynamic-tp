package org.dromara.dynamictp.example.controller;

import org.dromara.dynamictp.example.hystrix.HystrixTester;
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
    private HystrixTester hystrixTester;

    @GetMapping("/dtp-example-adapter/testHystrix")
    public String testHystrix() throws InterruptedException {
        return hystrixTester.testHystrix();
    }
}
