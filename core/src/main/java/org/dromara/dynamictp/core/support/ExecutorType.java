package org.dromara.dynamictp.core.support;

import org.dromara.dynamictp.core.thread.DtpExecutor;
import org.dromara.dynamictp.core.thread.ScheduledDtpExecutor;
import org.dromara.dynamictp.core.thread.EagerDtpExecutor;
import org.dromara.dynamictp.core.thread.OrderedDtpExecutor;
import lombok.Getter;

/**
 * ExecutorType related
 *
 * @author yanhom
 * @since 1.0.4
 **/
@Getter
public enum ExecutorType {

    /**
     * Executor type.
     */
    COMMON("common", DtpExecutor.class),
    EAGER("eager", EagerDtpExecutor.class),
    SCHEDULED("scheduled", ScheduledDtpExecutor.class),
    ORDERED("ordered", OrderedDtpExecutor.class);

    private final String name;

    private final Class<?> clazz;

    ExecutorType(String name, Class<?> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public static Class<?> getClass(String name) {
        for (ExecutorType type : ExecutorType.values()) {
            if (type.name.equals(name)) {
                return type.getClazz();
            }
        }
        return COMMON.getClazz();
    }
}
