package com.dtp.example.adapter.controller;

import com.dtp.example.adapter.dubbo.DubboUserService;
import com.dtp.example.adapter.grpc.GrpcClientService;
import com.dtp.example.adapter.hystrix.HystrixTester;
import com.dtp.example.adapter.okhttp3.Okhttp3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Redick01
 */
@Slf4j
@RestController
@SuppressWarnings("all")
public class TestController {

    @Resource
    private GrpcClientService grpcClientService;

    @Resource
    private DubboUserService dubboUserService;

    @Resource
    private HystrixTester hystrixTester;

    @Resource
    private Okhttp3Service okhttp3Service;

    @GetMapping("/dtp-example-adapter/testHystrix")
    public String testHystrix() throws InterruptedException {
        return hystrixTester.testHystrix();
    }

    @GetMapping("/dtp-example-adapter/testDubbo")
    public String testDubbo() throws InterruptedException {
        return dubboUserService.getUserName(1001);
    }

    @GetMapping("/dtp-example-adapter/testGrpc")
    public String testGrpc() throws InterruptedException {
        return grpcClientService.sendMessage("test dynamic tp");
    }

    @GetMapping("/dtp-example-adapter/testOkhttp3")
    public String testOkhttp3() throws InterruptedException {
        okhttp3Service.call("https://api.bilibili.com/x/web-interface/popular?ps=20&pn=1");
        return "";
    }
}
