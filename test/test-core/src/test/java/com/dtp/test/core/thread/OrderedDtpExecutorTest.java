package com.dtp.test.core.thread;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.dtp.core.spring.EnableDynamicTp;
import com.dtp.core.spring.YamlPropertySourceFactory;
import com.dtp.core.support.Ordered;
import com.dtp.core.support.callable.OrderedCallable;
import com.dtp.core.thread.OrderedDtpExecutor;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@PropertySource(value = "classpath:/dynamic-tp-nacos-demo-dtp-dev.yml", factory = YamlPropertySourceFactory.class)
@SpringBootTest(classes = OrderedDtpExecutorTest.class)
@ExtendWith(SpringExtension.class)
@EnableDynamicTp
@EnableAutoConfiguration
class OrderedDtpExecutorTest {

    @Resource
    private OrderedDtpExecutor orderedDtpExecutor;

    private final TransmittableThreadLocal<String> threadLocal = new TransmittableThreadLocal<>();

    private final List<String> TABLES = Lists.newArrayList("table1", "table2", "table3");

    @Test
    void orderedExecute() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            if (i == 500) {
                TimeUnit.MILLISECONDS.sleep(2000L);
            }
            threadLocal.set("test ordered execute " + i);
            MDC.put("traceId", String.valueOf(i));
            orderedDtpExecutor.execute(new TestOrderedRunnable("TEST"));
        }
//        new CountDownLatch(1).await();
    }

    @Test
    void orderedSubmit() {
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            threadLocal.set("ttl" + i);
            int tableIdx = ThreadLocalRandom.current().nextInt(3);
            Table table = new Table(TABLES.get(tableIdx), i);
            futures.add(orderedDtpExecutor.submit(new TestOrderedCallable(table)));
        }

        List<String> result = Lists.newArrayList();
        for (Future<?> future : futures) {
            try {
                result.add((String) future.get(2, TimeUnit.SECONDS));
            } catch (Exception e) {
                log.error("get future result error", e);
            }
        }
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

        public TestOrderedRunnable(String hashKey) {
            this.hashKey = hashKey;
        }

        @Override
        public Object hashKey() {
            return hashKey;
        }

        @Override
        public void run() {
            log.info("{} execute task, hashKey = {}, traceId = {}, threadLocalVal = {}",
                    Thread.currentThread().getName(), hashKey, MDC.get("traceId"), threadLocal.get());
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
