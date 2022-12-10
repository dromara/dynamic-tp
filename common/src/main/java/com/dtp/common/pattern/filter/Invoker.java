package com.dtp.common.pattern.filter;

/**
 * Invoker related
 *
 * @param <T> the param type
 * @author yanhom
 * @since 1.0.8
 **/
public interface Invoker<T> {

    /**
     * Invoke.
     *
     * @param context context
     */
    void invoke(T context);
}
