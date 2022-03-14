package com.dtp.core.support;

/**
 * TaskWrapper related
 *
 * @author: yanhom
 * @since 1.0.3
 **/
@FunctionalInterface
public interface TaskWrapper {

    /**
     * Enhance the given runnable.
     * @param runnable source runnable
     * @return target runnable
     */
    Runnable wrap(Runnable runnable);
}
