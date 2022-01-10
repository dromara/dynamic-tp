package io.lyh.dtp.core;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * BeanPostProcessor that handles all related beans managed by Spring,
 * mainly refers to the beans marked by @Bean annotation.
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class DtpPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        if (bean instanceof DtpExecutor) {
            DtpExecutor dtpExecutor = (DtpExecutor) bean;
            register(dtpExecutor);
            return dtpExecutor;
        }
        return bean;
    }

    private void register(DtpExecutor executor) {
        DtpRegistry.register(executor);
    }
}
