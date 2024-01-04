package org.dromara.dynamictp.core.executor.priority;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.FutureTask;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
@Slf4j
public class PriorityFutureTask<V> extends FutureTask<V> implements Comparable<PriorityFutureTask<V>> {

    /**
     * The underlying RunnableFuture
     */
    private final PriorityRunnable runnable;


    public PriorityFutureTask(Runnable runnable, V result) {
        super(runnable, result);
        this.runnable = (PriorityRunnable)runnable;
    }

    @Override
    public void run() {
        runnable.run();
    }

    @Override
    public int compareTo(PriorityFutureTask<V> o) {
        return Integer.compare(o.runnable.getPriority(), this.runnable.getPriority());
    }
}
