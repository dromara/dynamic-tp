package org.dromara.dynamictp.core.executor.priority;

import java.util.concurrent.Callable;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
public interface PriorityCallable<V> extends Callable<V> {

    int priority();

}
