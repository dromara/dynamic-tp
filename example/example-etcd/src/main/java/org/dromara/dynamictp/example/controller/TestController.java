package org.dromara.dynamictp.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Redick01
 */
@Slf4j
@RestController
@SuppressWarnings("all")
public class TestController {

    @Autowired
    @Qualifier("dtpExecutor1")
    private ThreadPoolExecutor dtpExecutor1;

    @Autowired
    @Qualifier("commonExecutor")
    private ThreadPoolExecutor commonExecutor;

    @GetMapping("/dtp-zookeeper-example/test")
    public String test() throws InterruptedException {
        task();
        return "success";
    }

    public void task() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            Thread.sleep(100);
            dtpExecutor1.execute(() -> {
                log.info("i am dynamic-tp-test-1 task");
            });
            commonExecutor.execute(() -> {
                log.info("i am commonExecutor task");
            });
        }
    }
}
