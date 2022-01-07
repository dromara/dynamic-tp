package io.lyh.dtp.domain;

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
     * jvm最大内存
     */
    private String maxMemory;

    /**
     * jvm已分配内存
     */
    private String totalMemory;

    /**
     * jvm已分配内存中的剩余空间
     */
    private String freeMemory;

    /**
     * jvm最大可用内容
     */
    private String usableMemory;
}
