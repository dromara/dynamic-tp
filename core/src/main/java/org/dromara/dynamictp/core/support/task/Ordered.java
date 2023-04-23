package org.dromara.dynamictp.core.support.task;

import org.dromara.dynamictp.core.thread.OrderedDtpExecutor;

/**
 * Used in {@link OrderedDtpExecutor} to ensure that the tasks are executed in order.
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
