package org.dromara.dynamictp.common.spring;


public interface EventListener<T extends Event> {
    void onEvent(T event);
}