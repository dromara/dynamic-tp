package io.lyh.dtp.core;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * DtpPostProcessor related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-27 16:57
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
        DtpKeeper.register(executor);
    }
}
