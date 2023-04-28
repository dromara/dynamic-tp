package org.dromara.dynamictp.example.controller;

import org.dromara.dynamictp.example.brpc.BrpcClientService;
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
    private BrpcClientService brpcClientService;

    @GetMapping("/dtp-example-adapter/testBrpc")
    public String testBrpc() throws InterruptedException {
        return brpcClientService.getUserName(111L);
    }
}
