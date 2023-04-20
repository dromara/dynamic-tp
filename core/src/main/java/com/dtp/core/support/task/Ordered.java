package com.dtp.core.support.task;

/**
 * Used in {@link com.dtp.core.thread.OrderedDtpExecutor} to ensure that the tasks are executed in order.
 *
 * @author yanhom
 * @since 1.0.0
 **/
public interface Ordered {

    /**
     * get hash key
     *
     * @return arg
     */
    Object hashKey();
}
