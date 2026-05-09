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

package org.dromara.dynamictp.test.core.support;

import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolStatProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * ThreadPoolStatProvider test
 *
 * @author yanhom
 * @since 1.2.2
 */
class ThreadPoolStatProviderTest {

    private DtpExecutor dtpExecutor;

    @AfterEach
    void tearDown() {
        if (dtpExecutor != null) {
            dtpExecutor.shutdownNow();
        }
    }

    @Test
    void testNullRunnableOperationsDoNotThrow() {
        ThreadPoolStatProvider provider = createProvider();

        assertDoesNotThrow(() -> provider.startTask(null));
        assertDoesNotThrow(() -> provider.completeTask(null));
        assertDoesNotThrow(() -> provider.cancelRunTimeoutTask(null));
        assertDoesNotThrow(() -> provider.cancelQueueTimeoutTask(null));
    }

    @Test
    void testStartAndCompleteTaskWorkNormally() {
        ThreadPoolStatProvider provider = createProvider();
        Runnable task = () -> {
        };

        assertDoesNotThrow(() -> {
            provider.startTask(task);
            TimeUnit.MILLISECONDS.sleep(5);
            provider.completeTask(task);
        });
    }

    private ThreadPoolStatProvider createProvider() {
        dtpExecutor = new DtpExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        dtpExecutor.setThreadPoolName("stat-provider-test");
        ExecutorWrapper wrapper = new ExecutorWrapper(dtpExecutor);
        return wrapper.getThreadPoolStatProvider();
    }
}
