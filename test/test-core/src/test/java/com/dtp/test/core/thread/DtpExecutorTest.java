package com.dtp.test.core.thread;

import com.dtp.core.DtpRegistry;
import com.dtp.core.spring.EnableDynamicTp;
import com.dtp.core.spring.YamlPropertySourceFactory;
import com.dtp.core.thread.DtpExecutor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * DtpExecutorTest related
 *
 * @author yanhom
 * @author kamtohung
 * @since 1.1.0
 */

@EnableDynamicTp
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DtpExecutorTest.class)
@PropertySource(value = "classpath:/dynamic-tp-nacos-demo-dtp-dev.yml", factory = YamlPropertySourceFactory.class)
public class DtpExecutorTest {

    @Test
    public void normal() {
        DtpExecutor dtpExecutor1 = DtpRegistry.getDtpExecutor("dtpExecutor1");
        dtpExecutor1.execute(() -> System.out.println("dtpExecutor1 execute"));
    }

}
