package com.dtp.core.spring;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.DtpRegistry;
import com.dtp.core.support.DynamicTp;
import com.dtp.core.support.TaskQueue;
import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.thread.EagerDtpExecutor;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.core.type.MethodMetadata;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * BeanPostProcessor that handles all related beans managed by Spring.
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {

        if (!(bean instanceof ThreadPoolExecutor) && !(bean instanceof ThreadPoolTaskExecutor)) {
            return bean;
        }

        if (bean instanceof DtpExecutor) {
            DtpExecutor dtpExecutor = (DtpExecutor) bean;
            if (bean instanceof EagerDtpExecutor) {
                ((TaskQueue) dtpExecutor.getQueue()).setExecutor((EagerDtpExecutor) dtpExecutor);
            }
            registerDtp(dtpExecutor);
            return dtpExecutor;
        }

        ApplicationContext applicationContext = ApplicationContextHolder.getInstance();
        String dtpAnnotationVal;
        try {
            DynamicTp dynamicTp = applicationContext.findAnnotationOnBean(beanName, DynamicTp.class);
            if (Objects.nonNull(dynamicTp)) {
                dtpAnnotationVal = dynamicTp.value();
            } else {
                BeanDefinitionRegistry registry = (BeanDefinitionRegistry) applicationContext;
                BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
                if (!(beanDefinition instanceof AnnotatedBeanDefinition)) {
                    return bean;
                }
                AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
                MethodMetadata methodMetadata = (MethodMetadata) annotatedBeanDefinition.getSource();
                if (Objects.isNull(methodMetadata) || !methodMetadata.isAnnotated(DynamicTp.class.getName())) {
                    return bean;
                }
                dtpAnnotationVal = Optional.ofNullable(methodMetadata.getAnnotationAttributes(DynamicTp.class.getName()))
                        .orElse(Collections.emptyMap())
                        .getOrDefault("value", "")
                        .toString();
            }
        } catch (NoSuchBeanDefinitionException e) {
            log.error("There is no bean with the given name {}", beanName, e);
            return bean;
        }

        String poolName = StringUtils.isNotBlank(dtpAnnotationVal) ? dtpAnnotationVal : beanName;
        if (bean instanceof ThreadPoolTaskExecutor) {
            registerCommon(poolName, ((ThreadPoolTaskExecutor) bean).getThreadPoolExecutor());
        } else {
            registerCommon(poolName, (ThreadPoolExecutor) bean);
        }
        return bean;
    }

    private void registerDtp(DtpExecutor executor) {
        DtpRegistry.registerDtp(executor, "beanPostProcessor");
    }

    private void registerCommon(String poolName, ThreadPoolExecutor executor) {
        ExecutorWrapper wrapper = new ExecutorWrapper(poolName, executor);
        DtpRegistry.registerCommon(wrapper, "beanPostProcessor");
    }
}
