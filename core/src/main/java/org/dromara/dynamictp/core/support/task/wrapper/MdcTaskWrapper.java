package org.dromara.dynamictp.core.support.task.wrapper;

import org.dromara.dynamictp.core.support.task.runnable.MdcRunnable;

/**
 * MdcTaskWrapper related
 *
 * @author yanhom
 * @since 1.0.8
 **/
public class MdcTaskWrapper implements TaskWrapper {

    private static final String NAME = "mdc";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Runnable wrap(Runnable runnable) {
        return MdcRunnable.get(runnable);
    }
}
