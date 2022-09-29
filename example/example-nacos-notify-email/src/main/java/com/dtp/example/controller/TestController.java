package com.dtp.example.controller;

import com.dtp.core.DtpRegistry;
import com.dtp.core.support.runnable.NamedRunnable;
import com.dtp.core.thread.DtpExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ljinfeng
 */
@Slf4j
@RestController
@SuppressWarnings("all")
public class TestController {

    @GetMapping("/dtp-nacos-notify-email-example/test")
    public String test() throws InterruptedException {
        task();
        return "success";
    }

    public void task() throws InterruptedException {
        DtpExecutor dtpExecutor1 = DtpRegistry.getDtpExecutor("dtpExecutor1");
        for (int i = 0; i < 100; i++) {
            dtpExecutor1.execute(NamedRunnable.of(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("dynamic-tp-test-1 task");
            }, "task-" + i));
        }
    }
}
