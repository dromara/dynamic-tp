package com.dtp.core.support.wrapper;

import com.alibaba.ttl.TtlRunnable;

/**
 * TtlTaskWrapper related
 *
 * @author: linyanhong@ihuman.com
 * @since 1.0.0
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
