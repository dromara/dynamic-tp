package com.dtp.example.config;

import com.dtp.common.em.QueueTypeEnum;
import com.dtp.core.support.DynamicTp;
import com.dtp.core.support.ThreadPoolCreator;
import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.support.ThreadPoolBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Redick01
 */
@Configuration
public class ThreadPoolConfiguration {

    /**
     * 通过{@link DynamicTp} 注解定义普通juc线程池，会享受到该框架监控功能，注解名称优先级高于方法名
     * @return 线程池实例
     */
    @DynamicTp("commonExecutor")
    @Bean
    public ThreadPoolExecutor commonExecutor() {
        return (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    }

//    /**
//     * 通过{@link ThreadPoolCreator} 快速创建一些简单配置的动态线程池
//     * @return 线程池实例
//     */
//    @Bean
//    public DtpExecutor dtpExecutor1() {
//        return ThreadPoolCreator.createDynamicFast("dtpExecutor1");
//    }

    /**
     * 通过{@link ThreadPoolBuilder} 设置详细参数创建动态线程池（推荐方式），
     * ioIntensive，参考tomcat线程池设计，实现了处理io密集型任务的线程池，具体参数可以看代码注释
     * @return 线程池实例
     */
    @Bean
    public DtpExecutor ioIntensiveExecutor() {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName("ioIntensiveExecutor")
                .corePoolSize(20)
                .maximumPoolSize(50)
                .queueCapacity(2048)
                .ioIntensive(true)
                .buildDynamic();
    }

//    @Bean
//    public ThreadPoolExecutor dtpExecutor2() {
//        return ThreadPoolBuilder.newBuilder()
//                .threadPoolName("dtpExecutor2")
//                .corePoolSize(10)
//                .maximumPoolSize(15)
//                .keepAliveTime(15000)
//                .timeUnit(TimeUnit.MILLISECONDS)
//                .workQueue(QueueTypeEnum.SYNCHRONOUS_QUEUE.getName(), null, false)
//                .waitForTasksToCompleteOnShutdown(true)
//                .awaitTerminationSeconds(5)
//                .buildDynamic();
//    }
}
