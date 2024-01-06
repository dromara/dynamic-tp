package org.dromara.dynamictp.test.core.thread;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.priority.PriorityDtpExecutor;
import org.dromara.dynamictp.core.spring.EnableDynamicTp;
import org.dromara.dynamictp.core.spring.YamlPropertySourceFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
@Slf4j
@EnableDynamicTp
@EnableAutoConfiguration
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
                TimeUnit.MILLISECONDS.sleep(100);
                log.info("work-{} completed successfully", number);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                countDownLatch.countDown();
            }
        }
    }

    @Test
    void submit() {
    }

    @Test
    void testSubmit() {
    }

    @Test
    void testSubmit1() {
    }

    @Test
    void testSubmit2() {
    }

    @Test
    void testSubmit3() {
    }

    @Test
    void testSubmit4() {
    }
}
