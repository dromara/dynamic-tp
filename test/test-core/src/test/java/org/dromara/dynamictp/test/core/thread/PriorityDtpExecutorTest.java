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
import org.dromara.dynamictp.core.executor.priority.PriorityDtpExecutor;
import org.dromara.dynamictp.spring.annotation.EnableDynamicTp;
import org.dromara.dynamictp.spring.support.YamlPropertySourceFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
@Slf4j
@EnableAutoConfiguration
@EnableDynamicTp
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PriorityDtpExecutorTest.class)
@PropertySource(value = "classpath:/dynamic-tp-demo.yml", factory = YamlPropertySourceFactory.class)
public class PriorityDtpExecutorTest {

    @Resource
    private PriorityDtpExecutor priorityDtpExecutor;

    @Test
    void execute() throws InterruptedException {
        int count = 5;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = count; i > 0; i--) {
            priorityDtpExecutor.execute(new TestPriorityRunnable(i, countDownLatch));
        }
        countDownLatch.await();
    }

    @Test
    void priorityExecute() throws InterruptedException {
        int count = 5;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = count; i > 0; i--) {
            priorityDtpExecutor.execute(new TestPriorityRunnable(i, countDownLatch), i);
        }
        countDownLatch.await();
    }

    @Test
    void submit() throws InterruptedException {
        int count = 5;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = count; i > 0; i--) {
            priorityDtpExecutor.submit(new TestPriorityRunnable(i, countDownLatch));
        }
        countDownLatch.await();
    }

    @Test
    void prioritySubmit() throws InterruptedException {
        int count = 5;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = count; i > 0; i--) {
            priorityDtpExecutor.submit(new TestPriorityRunnable(i, countDownLatch), i);
        }
        countDownLatch.await();
    }

    @Test
    void submitWithResult() throws InterruptedException, ExecutionException {
        int count = 5;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        List<Future<String>> list = new ArrayList<>();
        for (int i = count; i > 0; i--) {
            String name = "test-" + i;
            Future<String> result = priorityDtpExecutor.submit(new TestPriorityRunnable(i, countDownLatch), name);
            list.add(result);
        }
        countDownLatch.await();
        for (Future<String> future : list) {
            log.info("result: {}", future.get());
        }
    }

    @Test
    void prioritySubmitWithResult() throws InterruptedException, ExecutionException {
        int count = 5;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        List<Future<String>> list = new ArrayList<>();
        for (int i = count; i > 0; i--) {
            String name = "test-" + i;
            Future<String> result = priorityDtpExecutor.submit(new TestPriorityRunnable(i, countDownLatch), name, i);
            list.add(result);
        }
        countDownLatch.await();
        for (Future<String> future : list) {
            log.info("result: {}", future.get());
        }
    }

    @Test
    void submitCallable() throws InterruptedException, ExecutionException {
        int count = 5;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        List<Future<String>> list = new ArrayList<>();
        for (int i = count; i > 0; i--) {
            Future<String> result = priorityDtpExecutor.submit(new TestPriorityCallable(i, countDownLatch));
            list.add(result);
        }
        countDownLatch.await();
        for (Future<String> future : list) {
            log.info("result: {}", future.get());
        }
    }

    @Test
    void prioritySubmitCallable() throws InterruptedException, ExecutionException {
        int count = 5;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        List<Future<String>> list = new ArrayList<>();
        for (int i = count; i > 0; i--) {
            Future<String> result = priorityDtpExecutor.submit(new TestPriorityCallable(i, countDownLatch), i);
            list.add(result);
        }
        countDownLatch.await();
        for (Future<String> future : list) {
            log.info("result: {}", future.get());
        }
    }

    private static class TestPriorityRunnable implements Runnable {

        private final int number;

        private final CountDownLatch countDownLatch;

        public TestPriorityRunnable(int number, CountDownLatch countDownLatch) {
            this.number = number;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                log.info("work-{} triggered successfully", number);
                TimeUnit.MILLISECONDS.sleep(10);
                log.info("work-{} completed successfully", number);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                countDownLatch.countDown();
            }
        }
    }

    private static class TestPriorityCallable implements Callable<String> {

        private final int number;

        private final CountDownLatch countDownLatch;

        private TestPriorityCallable(int number, CountDownLatch countDownLatch) {
            this.number = number;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public String call() {
            try {
                log.info("work-{} triggered successfully", number);
                TimeUnit.MILLISECONDS.sleep(10);
                return "work-" + number;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                log.info("work-{} completed successfully", number);
                countDownLatch.countDown();
            }
        }
    }

}
