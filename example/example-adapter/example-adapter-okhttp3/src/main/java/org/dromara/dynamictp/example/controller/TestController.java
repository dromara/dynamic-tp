package org.dromara.dynamictp.example.controller;

import org.dromara.dynamictp.example.okhttp3.Okhttp3Service;
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
    private Okhttp3Service okhttp3Service;

    @GetMapping("/dtp-example-adapter/testOkhttp3")
    public String testOkhttp3() throws InterruptedException {
        okhttp3Service.call("https://api.bilibili.com/x/web-interface/popular?ps=20&pn=1");
        return "";
    }
}
