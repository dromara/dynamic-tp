package org.dromara.dynamictp.common.em;

import lombok.Getter;

/**
 * NotifyItemEnum related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Getter
public enum NotifyItemEnum {

    /**
     * Config change notify.
     */
    CHANGE("change"),

    /**
     * ThreadPool liveness notify.
     * liveness = activeCount / maximumPoolSize
     */
    LIVENESS("liveness"),

    /**
     * Capacity threshold notify
     */
    CAPACITY("capacity"),

    /**
     * Reject notify.
     */
    REJECT("reject"),

    /**
     * Task run timeout alarm.
     */
    RUN_TIMEOUT("run_timeout"),

    /**
     * Task queue wait timeout alarm.
     */
    QUEUE_TIMEOUT("queue_timeout");

    private final String value;

    NotifyItemEnum(String value) {
        this.value = value;
    }

    public static NotifyItemEnum of(String value) {
        for (NotifyItemEnum notifyItem : NotifyItemEnum.values()) {
            if (notifyItem.value.equals(value)) {
                return notifyItem;
            }
        }
        return null;
    }
}
