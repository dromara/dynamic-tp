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

package org.dromara.dynamictp.example.service.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.executor.OrderedDtpExecutor;
import org.dromara.dynamictp.core.support.task.runnable.NamedRunnable;
import org.dromara.dynamictp.core.support.task.runnable.OrderedRunnable;
import org.dromara.dynamictp.example.service.TestService;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * TestServiceImpl related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Slf4j
@Service
public class TestServiceImpl implements TestService {

    private final ThreadPoolExecutor jucThreadPoolExecutor;

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private final DtpExecutor eagerDtpExecutor;

    private final ScheduledExecutorService scheduledDtpExecutor;

    private final OrderedDtpExecutor orderedDtpExecutor;

    public TestServiceImpl(ThreadPoolExecutor jucThreadPoolExecutor,
                           ThreadPoolTaskExecutor threadPoolTaskExecutor,
                           DtpExecutor eagerDtpExecutor,
                           ScheduledExecutorService scheduledDtpExecutor,
                           OrderedDtpExecutor orderedDtpExecutor) {
        this.jucThreadPoolExecutor = jucThreadPoolExecutor;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.eagerDtpExecutor = eagerDtpExecutor;
        this.scheduledDtpExecutor = scheduledDtpExecutor;
        this.orderedDtpExecutor = orderedDtpExecutor;
    }

    @Override
    public void testJucTp() {
        for (int i = 0; i < 10; i++) {
            jucThreadPoolExecutor.execute(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.info("i am a jucThreadPoolExecutor's task");
            });
        }
    }

    @Override
    public void testSpringTp() {
        for (int i = 0; i < 10; i++) {
            threadPoolTaskExecutor.execute(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.info("i am a threadPoolTaskExecutor's task");
            });
        }
    }

    @Override
    public void testCommonDtp() {
        Executor dtpExecutor1 = DtpRegistry.getExecutor("dtpExecutor1");
        for (int i = 0; i < 10; i++) {
            dtpExecutor1.execute(NamedRunnable.of(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.info("i am a dtpExecutor's task");
            }, "task" + i));
        }
    }

    @Override
    public void testEagerDtp() {
        for (int i = 0; i < 10; i++) {
            eagerDtpExecutor.execute(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.info("i am a eagerDtpExecutor's task");
            });
        }
    }

    @Override
    public void testScheduledDtp() {
        scheduledDtpExecutor.scheduleAtFixedRate(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("i am a scheduledDtpExecutor's task");
        }, 0, 2, TimeUnit.SECONDS);
    }

    @Override
    public void testOrderedDtp() {
        for (int i = 0; i < 10; i++) {
            orderedDtpExecutor.execute(new TestOrderedRunnable(new UserInfo(i, "dtp" + i)));
        }
    }

    public static class TestOrderedRunnable implements OrderedRunnable {

        private final UserInfo userInfo;

        public TestOrderedRunnable(UserInfo userInfo) {
            this.userInfo = userInfo;
        }

        @Override
        public Object hashKey() {
            return userInfo.getUid();
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("i am a orderedDtpExecutor's task, userInfo: {}", userInfo);
        }
    }

    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private long uid;
        private String name;
    }
}
