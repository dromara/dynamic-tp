package org.dromara.dynamictp.core.executor.priority;


import lombok.Getter;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
public class PriorityRunnable implements Comparable<Object>, Runnable {

    private final Runnable runnable;

    @Getter
    private final int priority;

    private PriorityRunnable(Runnable runnable, int priority) {
        this.runnable = runnable;
        this.priority = priority;
    }

    @Override
    public void run() {
        this.runnable.run();
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof PriorityCallable) {
            return Integer.compare(this.priority, ((PriorityCallable<?>) o).getPriority());
        }
        return Integer.compare(this.priority, ((PriorityRunnable) o).priority);
    }

    public static PriorityRunnable of(Runnable runnable, int priority) {
        return new PriorityRunnable(runnable, priority);
    }

}
