package com.dtp.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * JvmMetrics related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class JvmMetrics extends Metrics {

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
