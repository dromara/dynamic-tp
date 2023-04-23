package org.dromara.dynamictp.core.support.task.callable;

import org.dromara.dynamictp.core.support.task.Ordered;

import java.util.concurrent.Callable;

/**
 * OrderedRunnable related
 *
 * @param <C> the result type of method
 * @author yanhom
 * @since 1.0.0
 **/
public interface OrderedCallable<C> extends Ordered, Callable<C> {

}
