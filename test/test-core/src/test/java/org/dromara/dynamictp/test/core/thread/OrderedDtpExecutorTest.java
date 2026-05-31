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

import com.alibaba.ttl.TransmittableThreadLocal;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.executor.OrderedDtpExecutor;
import org.dromara.dynamictp.core.support.task.Ordered;
import org.dromara.dynamictp.core.support.task.callable.OrderedCallable;
import org.dromara.dynamictp.spring.annotation.EnableDynamicTp;
import org.dromara.dynamictp.spring.support.YamlPropertySourceFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest(classes = OrderedDtpExecutorTest.class)
@ExtendWith(SpringExtension.class)
@EnableDynamicTp
@EnableAutoConfiguration
@PropertySource(value = "classpath:/dynamic-tp-nacos-demo-dtp-dev.yml", factory = YamlPropertySourceFactory.class)
class OrderedDtpExecutorTest {

    private final TransmittableThreadLocal<String> threadLocal = new TransmittableThreadLocal<>();

    private final List<String> TABLES = Lists.newArrayList("table1", "table2", "table3");

    @Test
    void orderedExecute() throws InterruptedException {
        OrderedDtpExecutor orderedDtpExecutor = (OrderedDtpExecutor) DtpRegistry.getExecutor("orderedDtpExecutor");
        assertNotNull(orderedDtpExecutor, "orderedDtpExecutor should be registered");
        int taskCount = 10;
        CountDownLatch latch = new CountDownLatch(taskCount);
        for (int i = 0; i < taskCount; i++) {
            threadLocal.set("test ordered execute " + i);
            MDC.put("traceId", String.valueOf(i));
            orderedDtpExecutor.execute(new TestOrderedRunnable("TEST", latch));
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS), "All ordered tasks should complete within timeout");
    }

    @Test
    void orderedSubmit() {
        OrderedDtpExecutor orderedDtpExecutor = (OrderedDtpExecutor) DtpRegistry.getExecutor("orderedDtpExecutor");
        assertNotNull(orderedDtpExecutor, "orderedDtpExecutor should be registered");
        int taskCount = 10;
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            threadLocal.set("ttl" + i);
            int tableIdx = ThreadLocalRandom.current().nextInt(3);
            Table table = new Table(TABLES.get(tableIdx), i);
            futures.add(orderedDtpExecutor.submit(new TestOrderedCallable(table)));
        }

        List<String> result = Lists.newArrayList();
        for (Future<?> future : futures) {
            try {
                result.add((String) future.get(5, TimeUnit.SECONDS));
            } catch (Exception e) {
                log.error("get future result error", e);
            }
        }
        assertTrue(result.size() == taskCount, "All submitted tasks should produce a result");
        result.forEach(r -> assertTrue(r != null && r.startsWith("table"), "Each result should start with 'table'"));
        log.info("result = {}", result);
    }

    @Data
    @AllArgsConstructor
    static class Table {
        private String name;
        private Object value;
    }

    class TestOrderedRunnable implements Ordered, Runnable {

        private final String hashKey;

        private final CountDownLatch latch;

        public TestOrderedRunnable(String hashKey) {
            this.hashKey = hashKey;
            this.latch = null;
        }

        public TestOrderedRunnable(String hashKey, CountDownLatch latch) {
            this.hashKey = hashKey;
            this.latch = latch;
        }

        @Override
        public Object hashKey() {
            return hashKey;
        }

        @Override
        public void run() {
            log.info("{} execute task, hashKey = {}, traceId = {}, threadLocalVal = {}",
                    Thread.currentThread().getName(), hashKey, MDC.get("traceId"), threadLocal.get());
            if (latch != null) {
                latch.countDown();
            }
        }
    }

    class TestOrderedCallable implements OrderedCallable<String> {

        private final Table table;

        public TestOrderedCallable(Table table) {
            this.table = table;
        }

        @Override
        public Object hashKey() {
            return table.getName();
        }

        @Override
        public String call() {
            log.info("{} execute task, threadLocalVal = {}, hashKey = {}, value = {}",
                    Thread.currentThread().getName(), threadLocal.get(), table.getName(), table.getValue());
            return table.getName() + table.getValue();
        }
    }
}
