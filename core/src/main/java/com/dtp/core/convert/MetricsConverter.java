package com.dtp.core.convert;

import com.dtp.common.dto.ThreadPoolStats;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * MetricsConverter related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class MetricsConverter {

    private MetricsConverter() {}

    public static ThreadPoolStats convert(ThreadPoolExecutor executor, String name, int rejectCount) {

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
                .rejectHandlerName(executor.getRejectedExecutionHandler().getClass().getSimpleName())
                .dtpName(name)
                .rejectCount(rejectCount)
                .build();
    }
}
