package com.dtp.common.pattern.filter;

/**
 * FilterChainFactory related
 *
 * @author yanhom
 * @since 1.0.8
 **/
public final class FilterChainFactory {

    private FilterChainFactory() { }

    @SafeVarargs
    public static<T> FilterChain<T> buildFilterChain(Invoker<T> target, Filter<T>... filters) {

        FilterChain<T> filterChain = new FilterChain<>();
        Invoker<T> last = target;
        for (int i = filters.length - 1; i >= 0; i--) {
            Invoker<T> next = last;
            Filter<T> filter = filters[i];
            last = new Invoker<T>() {
                @Override
                public void invoke(T context) {
                    filter.doFilter(context, next);
                }
            };
        }
        filterChain.setHead(last);
        return filterChain;
    }

}
