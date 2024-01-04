package org.dromara.dynamictp.core.executor.priority;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
@Slf4j
public class PriorityRunnable implements Comparable<PriorityRunnable>, Runnable {


    private final Runnable runnable;

    @Getter
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
    public int compareTo(PriorityRunnable o) {
        return Integer.compare(o.priority, this.priority);
    }

    public static PriorityRunnable of(Runnable runnable, int priority) {
        return new PriorityRunnable(runnable, priority);
    }

}
