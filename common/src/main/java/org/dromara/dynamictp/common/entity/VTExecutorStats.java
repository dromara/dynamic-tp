package org.dromara.dynamictp.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ClassName: VTExecutorStats
 * Package: org.dromara.dynamictp.common.entity
 * Description:
 *
 * @author CYC
 * @create 2024/11/4 16:52
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VTExecutorStats extends Metrics {

    /**
     * 虚拟线程执行器名字
     */
    private String executorName;

    /**
     * 虚拟线程执行器别名
     */
    private String executorAliasName;

    /**
     * 正在执行任务的活跃线程大致总数
     */
    private int activeCount;

    /**
     * 大致任务总数
     */
    private long taskCount;

    /**
     * 执行超时任务数量
     */
    private long runTimeoutCount;

    /**
     * 是否为DtpExecutor
     */
    private boolean dynamic;

    /**
     * tps
     */
    private double tps;

    /**
     * 最大任务耗时
     */
    private long maxRt;

    /**
     * 最小任务耗时
     */
    private long minRt;

    /**
     * 任务平均耗时(单位:ms)
     */
    private double avg;

    /**
     * 满足50%的任务执行所需的最低耗时
     */
    private double tp50;

    /**
     * 满足75%的任务执行所需的最低耗时
     */
    private double tp75;

    /**
     * 满足90%的任务执行所需的最低耗时
     */
    private double tp90;

    /**
     * 满足95%的任务执行所需的最低耗时
     */
    private double tp95;

    /**
     * 满足99%的任务执行所需的最低耗时
     */
    private double tp99;

    /**
     * 满足99.9%的任务执行所需的最低耗时
     */
    private double tp999;

}
