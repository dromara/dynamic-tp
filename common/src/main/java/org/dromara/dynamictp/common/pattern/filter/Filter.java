package org.dromara.dynamictp.common.pattern.filter;

/**
 * Filter related
 *
 * @param <T> the param type
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
     * @param nextInvoker next invoker
     */
    void doFilter(T context, Invoker<T> nextInvoker);
}
