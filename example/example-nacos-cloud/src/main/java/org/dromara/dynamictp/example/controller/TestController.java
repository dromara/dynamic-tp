/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.example.controller;

import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.support.task.runnable.NamedRunnable;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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

    @Resource
    private ThreadPoolExecutor dtpExecutor1;

    @GetMapping("/dtp-nacos-cloud-example/test")
    public String test() throws InterruptedException {
        task();
        return "success";
    }
    @GetMapping("/dtp12")
    public String test1() {
        System.out.println("333");
        return "success";
    }

    public void task() throws InterruptedException {
        MDC.put("traceId", UUID.randomUUID().toString());
        Executor dtpExecutor2 = DtpRegistry.getExecutor("supplierExecutor");
        for (int i = 0; i < 10; i++) {
            Thread.sleep(100);
            dtpExecutor1.execute(() -> {
                log.info("i am dynamic-tp-test-1 task, mdc: {}", MDC.get("traceId"));
            });
            dtpExecutor2.execute(NamedRunnable.of(() -> {
                try {
                    Thread.sleep(500);
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

