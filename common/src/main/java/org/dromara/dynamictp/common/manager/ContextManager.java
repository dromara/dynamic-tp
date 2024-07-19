package org.dromara.dynamictp.common.manager;

import java.util.Map;

public interface ContextManager {
    <T> T getBean(Class<T> clazz);
    <T> T getBean(String name, Class<T> clazz);
    <T> Map<String, T> getBeansOfType(Class<T> clazz);
    void setContext(Object context);
    void onEvent(Object event);
    Object getEnvironment();
    String getEnvironmentProperty(String key);
    String getEnvironmentProperty(String key, String defaultValue);
    String[] getActiveProfiles();
    String[] getDefaultProfiles();
}