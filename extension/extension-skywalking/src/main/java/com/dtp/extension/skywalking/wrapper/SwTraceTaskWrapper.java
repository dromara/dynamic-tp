package com.dtp.extension.skywalking.wrapper;

import com.dtp.core.support.wrapper.TaskWrapper;
import org.apache.skywalking.apm.toolkit.trace.RunnableWrapper;

/**
 * SwTraceTaskWrapper related
 *
 * @author yanhom
 * @since 1.0.8
 **/
public class SwTraceTaskWrapper implements TaskWrapper {

    private static final String NAME = "swTrace";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Runnable wrap(Runnable runnable) {
        return new RunnableWrapper(runnable);
    }
}
