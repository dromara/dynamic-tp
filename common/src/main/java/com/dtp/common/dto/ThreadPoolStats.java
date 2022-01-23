package com.dtp.common.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ThreadPoolStats related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class ThreadPoolStats extends Metrics {

    private String dtpName;

    private int corePoolSize;

    private int maximumPoolSize;

    private String queueType;

    private int queueCapacity;

    /**
     * 队列任务数量
     */
    private int queueSize;

    /**
     * SynchronousQueue队列模式
     */
    private boolean fair;

    /**
     * 队列剩余容量
     */
    private int queueRemainingCapacity;

    /**
     * 正在执行任务的活跃线程大致总数
     */
    private int activeCount;

    /**
     * 大致任务总数
     */
    private long taskCount;

    /**
     * 已执行完成的大致任务总数
     */
    private long completedTaskCount;

    /**
     * 池中曾经同时存在的最大线程数量
     */
    private int largestPoolSize;

    /**
     * 当前池中存在的线程总数
     */
    private int poolSize;

    /**
     * 等待执行的任务数量
     */
    private int waitTaskCount;

    /**
     * 拒绝的任务数量
     */
    private int rejectCount;

    /**
     * 拒绝策略名称
     */
    private String rejectHandlerName;

}