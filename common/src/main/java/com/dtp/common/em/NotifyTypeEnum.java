package com.dtp.common.em;

import lombok.Getter;

/**
 * NotifyTypeEnum related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Getter
public enum NotifyTypeEnum {

    /**
     * Config change notify.
     */
    CHANGE("change"),

    /**
     * ThreadPool livenes notify.
     * livenes = activeCount / maximumPoolSize
     */
    LIVENESS("liveness"),

    /**
     * Capacity threshold notify
     */
    CAPACITY("capacity"),

    /**
     * Reject notify.
     */
    REJECT("reject"),;

    private final String value;

    NotifyTypeEnum(String value) {
        this.value = value;
    }

    public static NotifyTypeEnum of(String value) {
        for (NotifyTypeEnum typeEnum : NotifyTypeEnum.values()) {
            if (typeEnum.value.equals(value)) {
                return typeEnum;
            }
        }
        return null;
    }
}
