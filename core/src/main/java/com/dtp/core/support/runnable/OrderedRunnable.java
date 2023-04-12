package com.dtp.core.support.runnable;

/**
 * OrderedRunnable related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public interface OrderedRunnable extends Runnable {

    /**
     * get hash key
     *
     * @return arg
     */
    Object hashKey();
}
