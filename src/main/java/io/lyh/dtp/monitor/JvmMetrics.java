package io.lyh.dtp.monitor;

import lombok.Data;

/**
 * JvmMetrics related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2022-01-06 15:52
 * @since 1.0.0
 **/
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
