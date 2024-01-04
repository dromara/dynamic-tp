package org.dromara.dynamictp.core.executor.priority;

import java.util.concurrent.Callable;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
public class PriorityCallable<V> implements Priority, Comparable<Object>, Callable<V> {

    private final Callable<V> callable;

    private final int priority;

    public PriorityCallable(Callable<V> callable, int priority) {
        this.callable = callable;
        this.priority = priority;
    }

    @Override
    public V call() throws Exception {
        return callable.call();
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public int compareTo(Object o) {
        return Integer.compare(((PriorityCallable<?>) o).priority, this.priority);
    }

}
