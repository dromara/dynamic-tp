package org.dromara.dynamictp.core.support.task.wrapper;

import com.alibaba.ttl.TtlRunnable;

/**
 * TtlTaskWrapper related
 *
 * @author yanhom
 * @since 1.0.4
 **/
public class TtlTaskWrapper implements TaskWrapper {

    private static final String NAME = "ttl";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Runnable wrap(Runnable runnable) {
        return TtlRunnable.get(runnable);
    }
}
