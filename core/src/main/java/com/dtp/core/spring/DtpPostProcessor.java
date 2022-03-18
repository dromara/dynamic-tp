package com.dtp.core.spring;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.core.DtpRegistry;
import com.dtp.core.support.DynamicTp;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.thread.DtpExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * BeanPostProcessor that handles all related beans managed by Spring,
 * mainly refers to the beans marked by @Bean annotation.
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        if (!(bean instanceof ThreadPoolExecutor)) {
            return bean;
        }

        if (bean instanceof DtpExecutor) {
            DtpExecutor dtpExecutor = (DtpExecutor) bean;
            registerDtp(dtpExecutor);
            return dtpExecutor;
        }

        ApplicationContext applicationContext = ApplicationContextHolder.getInstance();
        DynamicTp dynamicTp;
        try {
            dynamicTp = applicationContext.findAnnotationOnBean(beanName, DynamicTp.class);
            if (dynamicTp == null) {
                return bean;
            }
        } catch (NoSuchBeanDefinitionException e) {
            log.error("There is no bean with the given name {}", beanName, e);
            return bean;
        }

        String poolName = StringUtils.isNotBlank(dynamicTp.value()) ? dynamicTp.value() : beanName;
        registerCommon(poolName, (ThreadPoolExecutor) bean);
        return bean;
    }

    private void registerDtp(DtpExecutor executor) {
        DtpRegistry.registerDtp(executor, "beanPostProcessor");
    }

    private void registerCommon(String poolName, ThreadPoolExecutor executor) {
        ExecutorWrapper wrapper = new ExecutorWrapper();
        wrapper.setThreadPoolName(poolName);
        wrapper.setExecutor(executor);
        DtpRegistry.registerCommon(wrapper, "beanPostProcessor");
    }
}
