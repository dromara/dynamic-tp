package com.dtp.test.core.thread;

import com.dtp.core.DtpRegistry;
import com.dtp.core.spring.EnableDynamicTp;
import com.dtp.core.spring.YamlPropertySourceFactory;
import com.dtp.core.thread.ScheduledDtpExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@PropertySource(value = "classpath:/dynamic-tp-nacos-demo-dtp-dev.yml", factory = YamlPropertySourceFactory.class)
//获取启动类，加载配置，寻找主配置启动类 （被 @SpringBootApplication 注解的）
@SpringBootTest(classes = ScheduledDtpExecutorTest.class)
//让JUnit运行Spring的测试环境,获得Spring环境的上下文的支持
@ExtendWith(SpringExtension.class)
@EnableDynamicTp
@EnableAutoConfiguration
public class ScheduledDtpExecutorTest {

    @Test
    public void schedule() {
        ScheduledDtpExecutor dtpExecutor12 = (ScheduledDtpExecutor) DtpRegistry.getDtpExecutor("dtpExecutor12");
        System.out.println(dtpExecutor12.getClass());
        dtpExecutor12.scheduleAtFixedRate(() -> {
            System.out.println(Thread.currentThread().getName() + "进来了," +
                    "当前时间是 " + LocalDateTime.now());
        }, 10, 5, TimeUnit.SECONDS);
        dtpExecutor12.shutdownNow();
    }


    @Test
    public void testScheduleJre8Bug() {
        ScheduledDtpExecutor dtpExecutor13 = (ScheduledDtpExecutor) DtpRegistry.getDtpExecutor("dtpExecutor13");
        dtpExecutor13.scheduleAtFixedRate(() -> { }, 10, 5, TimeUnit.SECONDS);
        dtpExecutor13.shutdownNow();
    }

    @Test
    public void testSubNotify() {
        ScheduledDtpExecutor dtpExecutor14 = (ScheduledDtpExecutor) DtpRegistry.getDtpExecutor("dtpExecutor14");
        dtpExecutor14.scheduleAtFixedRate(() -> {
            System.out.println("进来了");
        }, 10, 5, TimeUnit.SECONDS);
        dtpExecutor14.shutdownNow();
    }

}
