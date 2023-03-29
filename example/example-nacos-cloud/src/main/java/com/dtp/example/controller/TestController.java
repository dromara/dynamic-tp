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
@Slf4j
@RestController
@SuppressWarnings("all")
public class TestController {

    @Resource
    private ThreadPoolExecutor dtpExecutor1;

    @GetMapping("/dtp-nacos-cloud-example/test")
    public String test() throws InterruptedException {
        task();
        return "success";
    }

    public void task() throws InterruptedException {
        MDC.put("traceId", UUID.randomUUID().toString());
        DtpExecutor dtpExecutor2 = DtpRegistry.getDtpExecutor("dtpExecutor2");
        for (int i = 0; i < 100; i++) {
            Thread.sleep(100);
            dtpExecutor1.execute(() -> {
                log.info("i am dynamic-tp-test-1 task, mdc: {}", MDC.get("traceId"));
            });
            dtpExecutor2.execute(NamedRunnable.of(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("i am dynamic-tp-test-2 task, mdc: {}", MDC.get("traceId"));
            }, "task-" + i));
        }
    }

    @GetMapping("/dtp-nacos-cloud-example/test-notify-run-timeout")
    public String testNotifyRunTimeout() {
        MDC.put("traceId", UUID.randomUUID().toString());
        DtpExecutor dtpExecutor2 = DtpRegistry.getDtpExecutor("dtpExecutor2");
        dtpExecutor2.execute(NamedRunnable.of(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("error", e);
            }
            log.info("i am dynamic-tp-test-2 task, mdc: {}", MDC.get("traceId"));
        }, "task-" + 0));
        return "success";
    }

}

