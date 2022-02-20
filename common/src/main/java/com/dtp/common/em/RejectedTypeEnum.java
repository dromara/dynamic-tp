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

    RejectedTypeEnum(String name) {
        this.name = name;
    }
}
