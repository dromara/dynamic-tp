package com.dtp.test.core.thread;

import com.dtp.core.DtpRegistry;
import com.dtp.core.spring.EnableDynamicTp;
import com.dtp.core.spring.YamlPropertySourceFactory;
import com.dtp.core.thread.ScheduledDtpExecutor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@PropertySource(value = "classpath:/dynamic-tp-nacos-demo-dtp-dev.yml", factory = YamlPropertySourceFactory.class)
//获取启动类，加载配置，寻找主配置启动类 （被 @SpringBootApplication 注解的）
@SpringBootTest(classes = ScheduledDtpExecutorTest.class)
//让JUnit运行Spring的测试环境,获得Spring环境的上下文的支持
@RunWith(SpringRunner.class)
@EnableDynamicTp
@EnableAutoConfiguration
public class ScheduledDtpExecutorTest {

    @Test
    public void schedule() {
        ScheduledDtpExecutor dtpExecutor12 = (ScheduledDtpExecutor) DtpRegistry.getDtpExecutor("dtpExecutor12");
        System.out.println(dtpExecutor12.getClass());
//        dtpExecutor12.schedule(() -> {
//            System.out.println(Thread.currentThread().getName() + "进来了," +
//                    "当前时间是 " + LocalDateTime.now());
//        }, 5, TimeUnit.SECONDS);

        dtpExecutor12.scheduleAtFixedRate(() -> {
            System.out.println(Thread.currentThread().getName() + "进来了," +
                    "当前时间是 " + LocalDateTime.now());
        }, 10, 5, TimeUnit.SECONDS);

//        dtpExecutor12.scheduleWithFixedDelay(() -> {
//            System.out.println(Thread.currentThread().getName() + "进来了," +
//                    "当前时间是 " + LocalDateTime.now());
//        }, 10, 5, TimeUnit.SECONDS);

//        dtpExecutor12.execute(() -> {
//            System.out.println(Thread.currentThread().getName() + "进来了," +
//                    "当前时间是 " + LocalDateTime.now());
//        });
    }

    @Test
    public void testScheduleJre8Bug() throws InterruptedException {
        ScheduledDtpExecutor dtpExecutor12 = (ScheduledDtpExecutor) DtpRegistry.getDtpExecutor("dtpExecutor13");
        dtpExecutor12.scheduleAtFixedRate(() -> {
        }, 10, 5, TimeUnit.SECONDS);
        new CountDownLatch(1).await();
    }

}
