package com.dtp.core.support.task.wrapper;

/**
 * TaskWrapper related
 *
 * @author yanhom
 * @since 1.0.3
 **/
@FunctionalInterface
public interface TaskWrapper {

    /**
     * Task wrapper name, for config.
     *
     * @return name
     */
    default String name() {
        return null;
    }

    /**
     * Enhance the given runnable.
     *
     * @param runnable source runnable
     * @return target runnable
     */
    Runnable wrap(Runnable runnable);
}
