package com.dtp.example.controller;

import com.dtp.core.DtpRegistry;
import com.dtp.core.support.runnable.NamedRunnable;
import com.dtp.core.thread.DtpExecutor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Redick01
 */
@RestController
@Slf4j
@SuppressWarnings("all")
public class TestController {

    @Resource
    private ThreadPoolExecutor dtpExecutor1;

    @GetMapping("/dtp-consul-example/test")
    public String test() throws InterruptedException {
        task();
        return "success";
    }

    public void task() throws InterruptedException {
        DtpExecutor dtpExecutor2 = DtpRegistry.getDtpExecutor("dtpExecutor2");
        MDC.put("traceId", UUID.randomUUID().toString());
        for (int i = 0; i < 100; i++) {
            Thread.sleep(100);
            dtpExecutor1.execute(() -> {
                log.info("i am dynamic-tp-test-1 task");
            });
            dtpExecutor2.execute(NamedRunnable.of(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("i am dynamic-tp-test-2 task");
            }, "task-" + i));
        }
    }
}
