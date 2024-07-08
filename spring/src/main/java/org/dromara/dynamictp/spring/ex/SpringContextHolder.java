package org.dromara.dynamictp.spring.ex;


import org.dromara.dynamictp.common.spring.ContextHolder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class SpringContextHolder implements ContextHolder, ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        return getInstance().getBean(clazz);
    }

    @Override
    public <T> T getBean(String name, Class<T> clazz) {
        return getInstance().getBean(name, clazz);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return getInstance().getBeansOfType(clazz);
    }


    public static ApplicationContext getInstance() {
        if (Objects.isNull(context)) {
            throw new NullPointerException("ApplicationContext is null, please check if the spring container is started.");
        }
        return context;
    }

    public static Environment getEnvironment() {
        return getInstance().getEnvironment();
    }

    @Override
    public void publishEvent(Object event) {
        if (event instanceof ApplicationEvent) {
            getInstance().publishEvent((ApplicationEvent) event);
        }
    }
}
