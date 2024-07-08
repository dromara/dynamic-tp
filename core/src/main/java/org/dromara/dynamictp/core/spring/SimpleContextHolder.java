package org.dromara.dynamictp.core.spring;


import org.dromara.dynamictp.common.spring.ContextHolder;
import org.dromara.dynamictp.common.spring.Event;
import org.dromara.dynamictp.common.spring.EventListener;
import org.dromara.dynamictp.common.spring.EventPublisher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SimpleContextHolder implements ContextHolder {

    private final Map<Class<?>, Object> beans = new ConcurrentHashMap<>();
    private final EventPublisher eventPublisher = new EventPublisher();

    @Override
    public <T> T getBean(Class<T> clazz) {
        return clazz.cast(beans.get(clazz));
    }

    @Override
    public <T> T getBean(String name, Class<T> clazz) {
        // 简单实现：假设name是类的全限定名
        try {
            Class<?> beanClass = Class.forName(name);
            return clazz.cast(beans.get(beanClass));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Bean not found: " + name, e);
        }
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return beans.entrySet().stream()
                .filter(entry -> clazz.isAssignableFrom(entry.getKey()))
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getName(),
                        entry -> clazz.cast(entry.getValue())));
    }

    @Override
    public void publishEvent(Object event) {
        if (event instanceof Event) {
            eventPublisher.publish((Event) event);
        }
    }

    public <T> void registerBean(Class<T> clazz, T bean) {
        beans.put(clazz, bean);
    }

    public void registerListener(EventListener<? extends Event> listener) {
        eventPublisher.registerListener(listener);
    }
}