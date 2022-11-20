package com.dtp.common.pattern.filter;

/**
 * Filter related
 *
 * @param <T> the source type
 * @author yanhom
 * @since 1.0.8
 **/
public interface Filter<T> {

    /**
     * Filter order.
     *
     * @return int val
     */
    int getOrder();

    /**
     * Do filter.
     *
     * @param context context
     * @param nextFilter next filter
     */
    void doFilter(T context, Invoker<T> nextFilter);
}
