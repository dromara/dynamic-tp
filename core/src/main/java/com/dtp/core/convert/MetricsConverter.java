package com.dtp.core.convert;

import com.dtp.common.dto.ThreadPoolStats;
import com.dtp.core.thread.DtpExecutor;

/**
 * MetricsConverter related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class MetricsConverter {

    private MetricsConverter() {}

    public static ThreadPoolStats convert(DtpExecutor executor) {

        if (executor == null) {
            return null;
        }
        return ThreadPoolStats.builder()
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(executor.getMaximumPoolSize())
                .queueType(executor.getQueue().getClass().getSimpleName())
                .queueCapacity(executor.getQueue().size() + executor.getQueue().remainingCapacity())
                .queueSize(executor.getQueue().size())
                .queueRemainingCapacity(executor.getQueue().remainingCapacity())
                .activeCount(executor.getActiveCount())
                .taskCount(executor.getTaskCount())
                .completedTaskCount(executor.getCompletedTaskCount())
                .largestPoolSize(executor.getLargestPoolSize())
                .poolSize(executor.getPoolSize())
                .waitTaskCount(executor.getQueue().size())
                .rejectHandlerName(executor.getRejectHandlerName())
                .dtpName(executor.getThreadPoolName())
                .rejectCount(executor.getRejectCount())
                .build();
    }
}
