package org.dromara.dynamictp.example.controller;

import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.support.task.runnable.NamedRunnable;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.Executor;
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

    @GetMapping("/dtp-nacos-cloud-example/test")
    public String test() throws InterruptedException {
        task();
        return "success";
    }

    public void task() throws InterruptedException {
        MDC.put("traceId", UUID.randomUUID().toString());
        Executor dtpExecutor2 = DtpRegistry.getExecutor("dtpExecutor2");
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
        Executor dtpExecutor2 = DtpRegistry.getExecutor("dtpExecutor2");
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

