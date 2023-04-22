package com.dtp.test.core.spring;

import com.dtp.core.spring.EnableDynamicTp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

/**
 * @author fei biao team
 * @version $
 * Date: 2023/4/22
 * Time: 14:27
 */
@EnableDynamicTp
@Configuration
public class Config {
    /**
     * 由于Aop依赖于DemoService。
     * AOP优先级高于BeanPostProcessor导致依赖服务DemoService被提前加载.
     * DemoService又依赖于asyncExecutor.所以导致asyncExecutor提前初始化完成
     * 解决办法：
     * 使用@Lazy或者将DtpPostProcessor的优先级提到PriorityOrdered
     *
     * @param asyncExecutor 线程池
     * @return DemoService
     * @see org.springframework.core.PriorityOrdered
     */
    @Bean
    //public DemoService demoService(@Lazy Executor asyncExecutor) {
    public DemoService demoService(Executor asyncExecutor) {
        return new DemoService(asyncExecutor);
    }
}
