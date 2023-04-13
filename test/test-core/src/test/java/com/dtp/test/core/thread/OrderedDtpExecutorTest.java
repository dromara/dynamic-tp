package com.dtp.test.core.thread;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.dtp.core.spring.EnableDynamicTp;
import com.dtp.core.spring.YamlPropertySourceFactory;
import com.dtp.core.support.Ordered;
import com.dtp.core.support.callable.OrderedCallable;
import com.dtp.core.thread.OrderedDtpExecutor;
import com.google.common.collect.Lists;
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
import java.util.UUID;
import java.util.concurrent.Future;
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

    @Test
    void orderedExecute() {
        for (int i = 0; i < 10; i++) {
            threadLocal.set("test ordered execute " + i);
            MDC.put("traceId", UUID.randomUUID().toString());
            orderedDtpExecutor.execute(new TestOrderedRunnable(String.valueOf(i)));
        }
    }

    @Test
    void orderedSubmit() {
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            threadLocal.set("test ordered submit " + i);
            MDC.put("traceId", UUID.randomUUID().toString());
            futures.add(orderedDtpExecutor.submit(new TestOrderedCallable(String.valueOf(i))));
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

        private final String hashKey;

        public TestOrderedCallable(String hashKey) {
            this.hashKey = hashKey;
        }

        @Override
        public Object hashKey() {
            return hashKey;
        }

        @Override
        public String call() {
            log.info("{} execute task, hashKey = {}, traceId = {}, threadLocalVal = {}",
                    Thread.currentThread().getName(), hashKey, MDC.get("traceId"), threadLocal.get());
            return UUID.randomUUID().toString();
        }
    }
}
