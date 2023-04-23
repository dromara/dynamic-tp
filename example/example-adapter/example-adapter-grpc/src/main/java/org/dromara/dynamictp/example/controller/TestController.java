package org.dromara.dynamictp.example.controller;

import org.dromara.dynamictp.example.grpc.GrpcClientService;
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
    private GrpcClientService grpcClientService;

    @GetMapping("/dtp-example-adapter/testGrpc")
    public String testGrpc() throws InterruptedException {
        return grpcClientService.sendMessage("test dynamic tp");
    }

}
