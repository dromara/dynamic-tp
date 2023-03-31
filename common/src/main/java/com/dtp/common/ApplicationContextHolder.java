package com.dtp.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.Objects;

/**
 * ApplicationContextHolder related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public class ApplicationContextHolder implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        if (Objects.isNull(context)) {
            throw new NullPointerException("ApplicationContext is null, please check if the spring container is started.");
        }
        return context.getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return context.getBean(name, clazz);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return context.getBeansOfType(clazz);
    }

    public static ApplicationContext getInstance() {
        return context;
    }

    public static Environment getEnvironment() {
        return getInstance().getEnvironment();
    }

    public static void publishEvent(ApplicationEvent event) {
        context.publishEvent(event);
    }
}
