package org.dromara.dynamictp.core.executor.priority;

import java.util.concurrent.Callable;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
public class PriorityCallable<V> implements Comparable<Object>, Callable<V> {

    private final Callable<V> callable;

    private final int priority;

    private PriorityCallable(Callable<V> callable, int priority) {
        this.callable = callable;
        this.priority = priority;
    }

    public static <T> Callable<T> of(Callable<T> task, int i) {
        return new PriorityCallable<>(task, i);
    }

    @Override
    public V call() throws Exception {
        return callable.call();
    }

    @Override
    public int compareTo(Object o) {
        return Integer.compare(this.priority, ((PriorityCallable<?>) o).priority);
    }

}
