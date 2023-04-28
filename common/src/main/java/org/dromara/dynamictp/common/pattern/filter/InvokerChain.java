package org.dromara.dynamictp.common.pattern.filter;

/**
 * InvokerChain related
 *
 * @author yanhom
 * @since 1.0.8
 **/
public class InvokerChain<T> {

    private Invoker<T> head;

    public void proceed(T context) {
        head.invoke(context);
    }

    public void setHead(Invoker<T> head) {
        this.head = head;
    }
}


