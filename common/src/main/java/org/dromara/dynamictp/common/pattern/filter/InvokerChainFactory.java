package org.dromara.dynamictp.common.pattern.filter;

/**
 * InvokerChainFactory related
 *
 * @author yanhom
 * @since 1.0.8
 **/
public final class InvokerChainFactory {

    private InvokerChainFactory() { }

    @SafeVarargs
    public static<T> InvokerChain<T> buildInvokerChain(Invoker<T> target, Filter<T>... filters) {

        InvokerChain<T> invokerChain = new InvokerChain<>();
        Invoker<T> last = target;
        for (int i = filters.length - 1; i >= 0; i--) {
            Invoker<T> next = last;
            Filter<T> filter = filters[i];
            last = context -> filter.doFilter(context, next);
        }
        invokerChain.setHead(last);
        return invokerChain;
    }
}


