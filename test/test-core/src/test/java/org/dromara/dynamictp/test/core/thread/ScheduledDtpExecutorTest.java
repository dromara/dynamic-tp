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

@PropertySource(value = "classpath:/dynamic-tp-nacos-demo-dtp-dev.yml", factory = YamlPropertySourceFactory.class)
//获取启动类，加载配置，寻找主配置启动类 （被 @SpringBootApplication 注解的）
@SpringBootTest(classes = ScheduledDtpExecutorTest.class)
//让JUnit运行Spring的测试环境,获得Spring环境的上下文的支持
@ExtendWith(SpringExtension.class)
@EnableDynamicTp
@EnableAutoConfiguration
class ScheduledDtpExecutorTest {

    @Test
    void schedule() {
        ScheduledDtpExecutor dtpExecutor12 = (ScheduledDtpExecutor) DtpRegistry.getExecutor("dtpExecutor12");
        System.out.println(dtpExecutor12.getClass());
        dtpExecutor12.scheduleAtFixedRate(() -> {
            System.out.println(Thread.currentThread().getName() + "进来了," +
                    "当前时间是 " + LocalDateTime.now());
        }, 10, 5, TimeUnit.SECONDS);
        dtpExecutor12.shutdownNow();
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
        dtpExecutor14.scheduleAtFixedRate(() -> {
            System.out.println("进来了");
        }, 10, 5, TimeUnit.SECONDS);
        dtpExecutor14.shutdownNow();
    }

    @Test
    void testScheduleCancel() {
        ScheduledDtpExecutor dtpExecutor12 = (ScheduledDtpExecutor) DtpRegistry.getExecutor("dtpExecutor12");
        ScheduledFuture<?> scheduledFuture = dtpExecutor12.scheduleWithFixedDelay(() -> {
            System.out.println(Thread.currentThread().getName() + "进来了," +
                    "当前时间是 ");
        }, 0, 1000, TimeUnit.MILLISECONDS);
        scheduledFuture.cancel(false);
    }

}
