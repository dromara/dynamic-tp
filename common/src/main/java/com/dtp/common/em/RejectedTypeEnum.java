package com.dtp.common.em;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * RejectedTypeEnum related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
@Getter
public enum RejectedTypeEnum {

    /**
     * RejectedExecutionHandler type while triggering reject policy.
     */
    ABORT_POLICY("AbortPolicy"),

    CALLER_RUNS_POLICY("CallerRunsPolicy"),

    DISCARD_OLDEST_POLICY("DiscardOldestPolicy"),

    DISCARD_POLICY("DiscardPolicy");

    private final String name;

    private static final String REJECTED_PREFIX = "RejectedCountable";

    RejectedTypeEnum(String name) {
        this.name = name;
    }

    public static String formatRejectName(String name) {
        if (name.startsWith(REJECTED_PREFIX)) {
            return name.replace(REJECTED_PREFIX, "");
        }
        return name;
    }
}
