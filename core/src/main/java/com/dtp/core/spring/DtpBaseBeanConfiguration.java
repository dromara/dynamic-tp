package com.dtp.core.spring;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.constant.DynamicTpConst;
import com.dtp.common.em.RejectedTypeEnum;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.timer.HashedWheelTimer;
import com.dtp.core.DtpRegistry;
import com.dtp.core.monitor.DtpEndpoint;
import com.dtp.core.monitor.DtpMonitor;
import com.dtp.core.support.DtpBannerPrinter;
import com.dtp.core.support.ThreadPoolBuilder;
import com.dtp.core.support.wrapper.TaskWrappers;
import com.dtp.core.thread.NamedThreadFactory;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Role;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.dtp.common.em.QueueTypeEnum.LINKED_BLOCKING_QUEUE;

/**
 * DtpBaseBeanConfiguration related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DtpProperties.class)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class DtpBaseBeanConfiguration {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public ApplicationContextHolder dtpApplicationContextHolder() {
        return new ApplicationContextHolder();
    }

    @Bean
    @DependsOn({"dtpApplicationContextHolder"})
    public DtpPostProcessor dtpPostProcessor() {
        return new DtpPostProcessor();
    }

    @Bean
    public DtpRegistry dtpRegistry() {
        return new DtpRegistry();
    }

    @Bean
    public DtpMonitor dtpMonitor() {
        return new DtpMonitor();
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public DtpEndpoint dtpEndpoint() {
        return new DtpEndpoint();
    }

    @Bean
    @ConditionalOnProperty(name = DynamicTpConst.BANNER_ENABLED_PROP, matchIfMissing = true, havingValue = "true")
    public DtpBannerPrinter dtpBannerPrinter() {
        return new DtpBannerPrinter();
    }

    @Bean
    public HashedWheelTimer hashedWheelTimer() {
        return new HashedWheelTimer(new NamedThreadFactory("dtpRunnable-timeout", true), 10, TimeUnit.MILLISECONDS);
    }

    @Bean(DynamicTpConst.ALARM_NAME)
    public ExecutorService alarmExecutor() {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName(DynamicTpConst.ALARM_NAME)
                .threadFactory(DynamicTpConst.ALARM_NAME)
                .corePoolSize(1)
                .maximumPoolSize(2)
                .workQueue(LINKED_BLOCKING_QUEUE.getName(), 2000, false, null)
                .rejectedExecutionHandler(RejectedTypeEnum.DISCARD_OLDEST_POLICY.getName())
                .taskWrappers(TaskWrappers.getInstance().getByNames(Sets.newHashSet("mdc")))
                .buildDynamic();
    }

}
