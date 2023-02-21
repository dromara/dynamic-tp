package com.dtp.example.controller;

import com.dtp.example.brpc.BrpcClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author fabian
 */
@Slf4j
@RestController
@SuppressWarnings("all")
public class TestController {

    @Resource
    private BrpcClientService brpcClientService;

    @GetMapping("/dtp-example-adapter/testBrpc")
    public String testBrpc() throws InterruptedException {
        return brpcClientService.getUserName(111L);
    }
}
