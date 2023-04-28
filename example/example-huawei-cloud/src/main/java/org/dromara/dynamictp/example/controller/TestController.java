package org.dromara.dynamictp.example.controller;

import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.support.task.runnable.NamedRunnable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.concurrent.Executor;

/**
 * @author windsearcher
 */
@Slf4j
@RestController
@SuppressWarnings("all")
public class TestController {


    @Autowired
    private DtpProperties dtpProperties;

    @GetMapping("/dtp-huawei-cloud-example/test")
    public String test() throws InterruptedException {
        task();
        return "Success";
    }

    public void task() throws InterruptedException {
        Executor dtpExecutor3 = DtpRegistry.getExecutor("dtpExecutor3");
        for (int i = 0; i < 100; i++) {
            Thread.sleep(100);

            dtpExecutor3.execute(NamedRunnable.of(() -> {
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
