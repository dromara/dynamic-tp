package com.dtp.core.helper;

import com.dtp.common.dto.ThreadPoolStats;
import com.dtp.core.DtpExecutor;

import static com.dtp.common.em.RejectedTypeEnum.formatRejectName;

/**
 * MetricsHelper related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class MetricsHelper {

    private MetricsHelper() {}

    public static ThreadPoolStats getPoolStats(DtpExecutor dtpExecutor) {
        ThreadPoolStats stats = new ThreadPoolStats();
        stats.setDtpName(dtpExecutor.getThreadPoolName());
        stats.setCorePoolSize(dtpExecutor.getCorePoolSize());
        stats.setMaximumPoolSize(dtpExecutor.getMaximumPoolSize());
        stats.setQueueType(dtpExecutor.getQueueName());
        stats.setQueueCapacity(dtpExecutor.getQueueCapacity());
        stats.setQueueSize(dtpExecutor.getQueue().size());
        stats.setQueueRemainingCapacity(dtpExecutor.getQueue().remainingCapacity());
        stats.setActiveCount(dtpExecutor.getActiveCount());
        stats.setTaskCount(dtpExecutor.getTaskCount());
        stats.setCompletedTaskCount(dtpExecutor.getCompletedTaskCount());
        stats.setLargestPoolSize(dtpExecutor.getLargestPoolSize());
        stats.setPoolSize(dtpExecutor.getPoolSize());
        stats.setWaitTaskCount(dtpExecutor.getQueue().size());
        stats.setRejectHandlerName(formatRejectName(dtpExecutor.getRejectHandlerName()));
        stats.setRejectCount(dtpExecutor.getRejectCount());
        return stats;
    }
}
