package com.dtp.core.support.runnable;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.MDC;

import java.util.Map;

import static com.dtp.common.constant.DynamicTpConst.TRACE_ID;

/**
 * MdcRunnable related
 *
 * @author yanhom
 * @since 1.0.8
 **/
public class MdcRunnable implements Runnable {

    private final Runnable runnable;

    /**
     * Saves the MDC value of the current thread
     */
    private final Map<String, String> parentMdc;

    public MdcRunnable(Runnable runnable) {
        this.runnable = runnable;
        this.parentMdc = MDC.getCopyOfContextMap();
    }

    public static MdcRunnable get(Runnable runnable) {
        return new MdcRunnable(runnable);
    }

    @Override
    public void run() {

        if (MapUtils.isEmpty(parentMdc)) {
            runnable.run();
            return;
        }

        // Assign the MDC value of the parent thread to the child thread
        for (Map.Entry<String, String> entry : parentMdc.entrySet()) {
            MDC.put(entry.getKey(), entry.getValue());
        }
        try {
            // Execute the decorated thread run method
            runnable.run();
        } finally {
            // Remove MDC value at the end of execution
            for (Map.Entry<String, String> entry : parentMdc.entrySet()) {
                if (!TRACE_ID.equals(entry.getKey())) {
                    MDC.remove(entry.getKey());
                }
            }
        }
    }
}
