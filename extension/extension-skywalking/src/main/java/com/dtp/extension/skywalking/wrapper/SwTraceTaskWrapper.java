package com.dtp.extension.skywalking.wrapper;

import com.dtp.core.support.runnable.MdcRunnable;
import com.dtp.core.support.wrapper.TaskWrapper;
import org.apache.skywalking.apm.toolkit.trace.RunnableWrapper;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.MDC;

import static com.dtp.common.constant.DynamicTpConst.SW_TRACE_ID;

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
        MDC.put(SW_TRACE_ID, TraceContext.traceId());
        return MdcRunnable.get(new RunnableWrapper(runnable));
    }
}
