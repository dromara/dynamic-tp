package com.dtp.common.pattern.filter;

/**
 * FilterChain related
 *
 * @author yanhom
 * @since 1.0.8
 **/
public class FilterChain<T> {

    private Invoker<T> head;

    public void fire(T context) {
        head.invoke(context);
    }

    public void setHead(Invoker<T> head) {
        this.head = head;
    }
}
