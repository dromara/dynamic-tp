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

package org.dromara.dynamictp.test.core.thread;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.executor.ScheduledDtpExecutor;
import org.dromara.dynamictp.spring.annotation.EnableDynamicTp;
import org.dromara.dynamictp.spring.support.YamlPropertySourceFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@PropertySource(value = "classpath:/dynamic-tp-nacos-demo-dtp-dev.yml", factory = YamlPropertySourceFactory.class)
@SpringBootTest(classes = ScheduledDtpExecutorTest.class)
@ExtendWith(SpringExtension.class)
@EnableDynamicTp
@EnableAutoConfiguration
class ScheduledDtpExecutorTest {

    @Test
    void schedule() {
        ScheduledDtpExecutor dtpExecutor12 = (ScheduledDtpExecutor) DtpRegistry.getExecutor("dtpExecutor12");
        dtpExecutor12.scheduleAtFixedRate(() ->
                log.info("schedule task running at {}", LocalDateTime.now()),
                10, 5, TimeUnit.SECONDS);
        dtpExecutor12.shutdownNow();
        assertTrue(dtpExecutor12.isShutdown());
    }

    @Test
    void testScheduleJre8Bug() {
        ScheduledDtpExecutor dtpExecutor13 = (ScheduledDtpExecutor) DtpRegistry.getExecutor("dtpExecutor13");
        dtpExecutor13.scheduleAtFixedRate(() -> { }, 10, 5, TimeUnit.SECONDS);
        dtpExecutor13.shutdownNow();
    }

    @Test
    void testSubNotify() {
        ScheduledDtpExecutor dtpExecutor14 = (ScheduledDtpExecutor) DtpRegistry.getExecutor("dtpExecutor14");
        dtpExecutor14.scheduleAtFixedRate(() -> { }, 10, 5, TimeUnit.SECONDS);
        dtpExecutor14.shutdownNow();
        assertTrue(dtpExecutor14.isShutdown());
    }

    @Test
    void testScheduleCancel() {
        ScheduledDtpExecutor dtpExecutor12 = (ScheduledDtpExecutor) DtpRegistry.getExecutor("dtpExecutor12");
        ScheduledFuture<?> scheduledFuture = dtpExecutor12.scheduleWithFixedDelay(() -> { },
                0, 1000, TimeUnit.MILLISECONDS);
        scheduledFuture.cancel(false);
        assertTrue(scheduledFuture.isCancelled());
    }

}
