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
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.spring.annotation.EnableDynamicTp;
import org.dromara.dynamictp.spring.support.YamlPropertySourceFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mockStatic;

/**
 * DtpExecutorTest related
 *
 * @author yanhom
 * @author kamtohung
 * @since 1.1.0
 */
@Slf4j
@EnableDynamicTp
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DtpExecutorTest.class)
@PropertySource(value = "classpath:/dynamic-tp-demo.yml", factory = YamlPropertySourceFactory.class)
public class DtpExecutorTest {

    public void mock(MockedStatic<AlarmManager> mockAlarmManager) {
        mockAlarmManager.when(() -> AlarmManager.tryAlarmAsync(any(), any(), any())).then(invocation -> null);
        mockAlarmManager.when(() -> AlarmManager.tryAlarmAsync(any(ExecutorWrapper.class), anyList())).then(invocation -> null);
    }

    @RepeatedTest(100)
    void testRunTimeout() {
        Executor dtpExecutor = DtpRegistry.getExecutor("testRunTimeoutDtpExecutor");
        dtpExecutor.execute(() -> {
            try (MockedStatic<AlarmManager> mockAlarmManager = mockStatic(AlarmManager.class)) {
                mock(mockAlarmManager);
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                // ignore
            }
        });
    }

    @RepeatedTest(100)
    void testQueueTimeout() {
        Executor dtpExecutor = DtpRegistry.getExecutor("testQueueTimeoutDtpExecutor");
        dtpExecutor.execute(() -> {
            try (MockedStatic<AlarmManager> mockAlarmManager = mockStatic(AlarmManager.class)) {
                mock(mockAlarmManager);
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                // ignore
            }
        });
    }

    @RepeatedTest(100)
    void testRejectedQueueTimeoutCancel() {
        Executor dtpExecutor = DtpRegistry.getExecutor("testRejectedQueueTimeoutCancelDtpExecutor");
        dtpExecutor.execute(() -> {
            try (MockedStatic<AlarmManager> mockAlarmManager = mockStatic(AlarmManager.class)) {
                mock(mockAlarmManager);
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                // ignore
            }
        });
    }

    @AfterAll
    public static void afterAll() throws InterruptedException {
//        TimeUnit.SECONDS.sleep(100);
    }
}
