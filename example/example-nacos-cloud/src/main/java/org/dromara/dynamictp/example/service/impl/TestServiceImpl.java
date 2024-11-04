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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

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

    private final ExecutorService virtualThreadExecutor;

    public TestServiceImpl(ThreadPoolExecutor jucThreadPoolExecutor,
                           ThreadPoolTaskExecutor threadPoolTaskExecutor,
                           DtpExecutor eagerDtpExecutor,
                           ScheduledExecutorService scheduledDtpExecutor,
                           OrderedDtpExecutor orderedDtpExecutor,
                           @Qualifier("VirtualThreadExecutor1") ExecutorService virtualThreadExecutor) {
        this.jucThreadPoolExecutor = jucThreadPoolExecutor;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.eagerDtpExecutor = eagerDtpExecutor;
        this.scheduledDtpExecutor = scheduledDtpExecutor;
        this.orderedDtpExecutor = orderedDtpExecutor;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

//    public TestServiceImpl(@Qualifier("VirtualThreadExecutor1") ExecutorService virtualThreadExecutor) {
//        this.VirtualThreadExecutor = virtualThreadExecutor;
//    }

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

    @Override
    public void testVTExecutor() {
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            virtualThreadExecutor.execute(() -> {
                log.info("i am a VTExecutor's {} task", finalI);
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("i am a VTExecutor's task");

            });
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
