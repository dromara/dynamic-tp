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

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.ScheduledDtpExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author kyao
 */
@Slf4j
@RestController
@SuppressWarnings("all")
public class TestController {

    @Resource
    private ScheduledDtpExecutor testExecutor;

    @Resource
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    @GetMapping("/dtp-example-adapter/testWebserver")
    public String testWebserver() throws InterruptedException {
        testExecutor.schedule(() -> {
            try {
                Thread.sleep((int) (Math.random() * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("success");
        }, 1, TimeUnit.SECONDS);
        return "success";
    }

    @GetMapping("/dtp-example-adapter/testScheduleExecutor")
    public String testScheduleExecutor() throws InterruptedException {
        scheduledThreadPoolExecutor.schedule(() -> {
            try {
                Thread.sleep((int) (Math.random() * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("success");
        }, 1, TimeUnit.SECONDS);
        Thread.sleep(2000);
        return "success";
    }
}
