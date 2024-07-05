package org.dromara.dynamictp.common.spring;

import java.util.Map;

public interface BeanProvider {
    <T> T getBean(Class<T> clazz);
    <T> Map<String, T> getBeansOfType(Class<T> clazz);
}