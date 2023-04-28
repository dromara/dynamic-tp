package org.dromara.dynamictp.test.core.thread;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.spring.EnableDynamicTp;
import org.dromara.dynamictp.core.spring.YamlPropertySourceFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * EagerDtpExecutorTest related
 *
 * @author kamtohung
 * @since 1.1.0
 */
@Slf4j
@EnableDynamicTp
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EagerDtpExecutorTest.class)
@PropertySource(value = "classpath:/dynamic-tp-demo.yml", factory = YamlPropertySourceFactory.class)
class EagerDtpExecutorTest {

    @Test
    void test() throws InterruptedException {
        Executor executor = DtpRegistry.getExecutor("eagerDtpThreadPoolExecutor");
        for (int i = 0; i < 10; i++) {
            executor.execute(() -> {
                try {
                    TimeUnit.SECONDS.sleep(300L);
                } catch (InterruptedException e) {

                }
            });
        }
    }
}
