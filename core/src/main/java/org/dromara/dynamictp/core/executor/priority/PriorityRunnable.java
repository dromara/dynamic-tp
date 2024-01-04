package org.dromara.dynamictp.core.executor.priority;


/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
public class PriorityRunnable implements Priority, Comparable<Object>, Runnable {

    private final Runnable runnable;

    private final int priority;

    public PriorityRunnable(Runnable runnable, int priority) {
        this.runnable = runnable;
        this.priority = priority;
    }

    @Override
    public void run() {
        this.runnable.run();
    }

    @Override
    public int compareTo(Object o) {
        return Integer.compare(((PriorityRunnable) o).priority, this.priority);
    }

    public static PriorityRunnable of(Runnable runnable, int priority) {
        return new PriorityRunnable(runnable, priority);
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

}
