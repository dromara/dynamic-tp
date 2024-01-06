package org.dromara.dynamictp.core.executor.priority;


import lombok.var;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
public class PriorityFutureTask<V> extends FutureTask<V> implements Priority {

    /**
     * The runnable.
     */
    private final Priority obj;

    private final int priority;

    public PriorityFutureTask(Runnable runnable, V result) {
        super(runnable, result);
        this.obj = (PriorityRunnable)runnable;
        this.priority = this.obj.getPriority();
    }

    public PriorityFutureTask(Callable<V> callable) {
        super(callable);
        this.obj = (PriorityCallable<V>)callable;
        this.priority = this.obj.getPriority();
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

}
