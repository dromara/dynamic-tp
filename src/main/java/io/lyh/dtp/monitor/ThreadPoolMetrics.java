package io.lyh.dtp.monitor;

import lombok.Data;

/**
 * ThredPoolMetrics related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Data
public class ThreadPoolMetrics extends Metrics {

    private String dtpName;

    private Integer corePoolSize;

    private Integer maximumPoolSize;

    private String queueType;

    private Integer queueCapacity;

    /**
     * 队列任务数量
     */
    private Integer queueSize;

    /**
     * SynchronousQueue队列模式
     */
    private boolean fair;

    /**
     * 队列剩余容量
     */
    private Integer queueRemainingCapacity;

    /**
     * 正在执行任务的活跃线程大致总数
     */
    private Integer activeCount;

    /**
     * 大致任务总数
     */
    private Long taskCount;

    /**
     * 已执行完成的大致任务总数
     */
    private Long completedTaskCount;

    /**
     * 池中曾经同时存在的最大线程数量
     */
    private Integer largestPoolSize;

    /**
     * 当前池中存在的线程总数
     */
    private Integer poolSize;

    /**
     * 等待执行的任务数量
     */
    private Integer waitTaskCount;

    /**
     * 拒绝的任务数量
     */
    private Integer rejectCount;

    /**
     * 拒绝策略名称
     */
    private String rejectHandlerName;

}
