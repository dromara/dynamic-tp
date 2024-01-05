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

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
@Slf4j
@EnableDynamicTp
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EagerDtpExecutorTest.class)
@PropertySource(value = "classpath:/dynamic-tp-demo.yml", factory = YamlPropertySourceFactory.class)
public class PriorityDtpExecutorTest {

    @Resource
    private PriorityDtpExecutor priorityDtpExecutor;

    @Test
    void execute() {
        priorityDtpExecutor.execute(() -> {
            log.info("test");
        });
    }

    @Test
    void testExecute() {
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
