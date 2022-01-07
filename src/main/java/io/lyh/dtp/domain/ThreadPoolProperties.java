package io.lyh.dtp.domain;

import io.lyh.dtp.common.em.QueueTypeEnum;
import io.lyh.dtp.common.em.RejectedTypeEnum;
import io.lyh.dtp.common.constant.DynamicTpConst;
import lombok.Data;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ThreadPoolProperties related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-27 17:37
 * @since 1.0.0
 **/
@Data
public class ThreadPoolProperties {

    /**
     * 线程池名称
     */
    private String threadPoolName = "DynamicTp";

    /**
     * 核心线程数
     */
    private int corePoolSize = 2;

    /**
     * 最大线程数, 默认值为CPU核心数量
     */
    private int maximumPoolSize = DynamicTpConst.AVAILABLE_PROCESSORS;

    /**
     * 队列最大数量
     */
    private int queueCapacity = 1024;

    /**
     * 队列类型
     * @see QueueTypeEnum
     */
    private String queueType = QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE.getName();

    /**
     * 是否公平策略 SynchronousQueue用
     */
    private boolean fair = false;

    /**
     * 拒绝策略
     * @see RejectedTypeEnum
     */
    private String rejectedExecutionName = RejectedTypeEnum.CALLER_RUNS_POLICY.getName();

    /**
     * 空闲线程存活时间
     */
    private long keepAliveTime = 30;

    /**
     * 空闲线程存活时间单位
     */
    private TimeUnit unit = TimeUnit.SECONDS;

    /**
     * 是否允许核心线程超时
     */
    private boolean allowCoreThreadTimeOut = false;

    /**
     * 线程名称前缀
     */
    private String threadNamePrefix = "dynamic-tp";

    /**
     * 报警配置
     */
    private List<NotifyItem> notifyItems;

}
