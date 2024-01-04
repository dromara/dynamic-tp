package org.dromara.dynamictp.core.executor.priority;


import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
public class PriorityFutureTask<V> extends FutureTask<V> implements Comparable<Object> {

    /**
     * The runnable.
     */
    private final Comparable<Object> obj;

    public PriorityFutureTask(Runnable runnable, V result) {
        super(runnable, result);
        this.obj = (PriorityRunnable)runnable;
    }

    public PriorityFutureTask(Callable<V> callable) {
        super(callable);
        this.obj = (PriorityCallable<V>)callable;
    }

    @Override
    public int compareTo(Object o) {
        return this.obj.compareTo(((PriorityFutureTask<?>)o).obj);
    }

}
