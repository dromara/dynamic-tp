package org.dromara.dynamictp.common.spring;

import java.util.Map;

public interface ContextHolder {
    <T> T getBean(Class<T> clazz);
    <T> T getBean(String name, Class<T> clazz);
    <T> Map<String, T> getBeansOfType(Class<T> clazz);
    void publishEvent(Object event);
}