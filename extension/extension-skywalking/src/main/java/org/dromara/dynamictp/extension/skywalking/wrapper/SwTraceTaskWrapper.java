package org.dromara.dynamictp.extension.skywalking.wrapper;

import org.dromara.dynamictp.core.support.task.runnable.MdcRunnable;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;
import org.apache.skywalking.apm.toolkit.trace.RunnableWrapper;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.MDC;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.TRACE_ID;

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
        MDC.put(TRACE_ID, TraceContext.traceId());
        return MdcRunnable.get(new RunnableWrapper(runnable));
    }
}
