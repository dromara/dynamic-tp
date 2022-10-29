package com.dtp.example.adapter.controller;

import com.dtp.example.adapter.dubbo.DubboUserService;
import com.dtp.example.adapter.grpc.GrpcClientService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
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

    @HystrixCommand(
            threadPoolKey = "testThreadPoolKey",
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.strategy", value = "THREAD"),
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000"),
                    @HystrixProperty(name = "circuitBreaker.enabled", value = "true"),
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "200")
            },
            threadPoolProperties = {
                    @HystrixProperty(name = "coreSize", value = "5"),
                    @HystrixProperty(name = "maxQueueSize", value = "10")
            }
    )
    @GetMapping("/dtp-example-adapter/testHystrix")
    public String testHystrix() throws InterruptedException {
        return "success";
    }

    @GetMapping("/dtp-example-adapter/testDubbo")
    public String testDubbo() throws InterruptedException {
        return dubboUserService.getUserName(1001);
    }

    @GetMapping("/dtp-example-adapter/testGrpc")
    public String testGrpc() throws InterruptedException {
        return grpcClientService.sendMessage("test dynamic tp");
    }
}
