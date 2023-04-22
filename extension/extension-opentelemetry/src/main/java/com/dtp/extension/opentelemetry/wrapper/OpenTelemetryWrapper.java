package com.dtp.extension.opentelemetry.wrapper;

import com.dtp.core.support.task.runnable.MdcRunnable;
import com.dtp.core.support.task.wrapper.TaskWrapper;
import io.opentelemetry.context.Context;

/**
 * OpenTelemetryWrapper related
 *
 * @author weishaopeng
 * @since 1.0.8
 **/
public class OpenTelemetryWrapper implements TaskWrapper {

    private static final String NAME = "OTel";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Runnable wrap(Runnable runnable) {
        Context context = Context.current();
        // 被wrap方法包装后，该Executor执行的所有Runnable都会跑在特定的context中
        return MdcRunnable.get(context.wrap(runnable));
    }
}
