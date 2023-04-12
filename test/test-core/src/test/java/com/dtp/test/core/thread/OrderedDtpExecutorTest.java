package com.dtp.test.core.thread;

import com.dtp.core.DtpRegistry;
import com.dtp.core.spring.EnableDynamicTp;
import com.dtp.core.spring.YamlPropertySourceFactory;
import com.dtp.core.support.runnable.OrderedRunnable;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;
import java.util.concurrent.Executor;

@Slf4j
@PropertySource(value = "classpath:/dynamic-tp-nacos-demo-dtp-dev.yml", factory = YamlPropertySourceFactory.class)
@SpringBootTest(classes = OrderedDtpExecutorTest.class)
@ExtendWith(SpringExtension.class)
@EnableDynamicTp
@EnableAutoConfiguration
class OrderedDtpExecutorTest {

    @Test
    void orderedExecute() {
        Executor orderedDtpExecutor = DtpRegistry.getDtpExecutor("orderedDtpExecutor");
        for (int i = 0; i < 10; i++) {
            MDC.put("traceId", UUID.randomUUID().toString());
            orderedDtpExecutor.execute(new TestOrderedRunnable(String.valueOf(i)));
        }
    }

    static class TestOrderedRunnable implements OrderedRunnable {

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
            log.info("{} execute task, hashKey = {}, traceId = {}", Thread.currentThread().getName(), hashKey, MDC.get("traceId"));
        }
    }
}
