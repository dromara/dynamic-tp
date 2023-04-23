package com.dtp.test.core.spring;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.interceptor.CustomizableTraceInterceptor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author fei biao team
 * @version $
 * Date: 2023/4/22
 * Time: 15:03
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AopConfig {
    /**
     * 注意：依赖点demoService。
     * Aop优先级高于BeanPostProcessor导致依赖服务DemoService被提前加载.
     * DemoService又依赖于asyncExecutor.所以导致线程池提前初始化完成
     *
     * @param demoService 依赖bean
     * @return Advisor
     */
    @Bean
    public Advisor advisor(DemoService demoService) {
        //just demo.Just discard.
        AspectJExpressionPointcut aspectJExpressionPointcut = new AspectJExpressionPointcut();
        aspectJExpressionPointcut.setExpression("execution(public * com.dtp.test.core.spring.Nothing.test(..))");
        return new DefaultPointcutAdvisor(aspectJExpressionPointcut,
                new CustomizableTraceInterceptor());
    }
}
