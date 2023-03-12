package com.dtp.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * JvmStats related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class JvmStats extends Metrics {

    /**
     * Jvm max memory.
     */
    private String maxMemory;

    /**
     * Jvm total memory.
     */
    private String totalMemory;

    /**
     * Jvm free memory.
     */
    private String freeMemory;

    /**
     * Jvm usable memory.
     */
    private String usableMemory;
}
