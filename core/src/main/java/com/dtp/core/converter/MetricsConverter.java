package com.dtp.core.converter;

import com.dtp.common.entity.ThreadPoolStats;
import com.dtp.core.support.ExecutorAdapter;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.thread.DtpExecutor;

/**
 * MetricsConverter related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public class MetricsConverter {

    private MetricsConverter() {
    }

    public static ThreadPoolStats convert(ExecutorWrapper wrapper) {
        ExecutorAdapter<?> executor = wrapper.getExecutor();
        if (executor == null) {
            return null;
        }
        ThreadPoolStats poolStats = convertCommon(executor);
        poolStats.setPoolName(wrapper.getThreadPoolName());
        if (executor instanceof DtpExecutor) {
            DtpExecutor dtpExecutor = (DtpExecutor) executor;
            poolStats.setRejectHandlerName(dtpExecutor.getRejectHandlerName());
            poolStats.setRejectCount(dtpExecutor.getRejectCount());
            poolStats.setRunTimeoutCount(dtpExecutor.getRunTimeoutCount());
            poolStats.setQueueTimeoutCount(dtpExecutor.getQueueTimeoutCount());
            poolStats.setDynamic(true);
        } else {
            poolStats.setDynamic(false);
        }
        return poolStats;
    }

    public static ThreadPoolStats convertCommon(ExecutorAdapter<?> executor) {
        return ThreadPoolStats.builder()
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(executor.getMaximumPoolSize())
                .poolSize(executor.getPoolSize())
                .activeCount(executor.getActiveCount())
                .largestPoolSize(executor.getLargestPoolSize())
                .queueType(executor.getQueue().getClass().getSimpleName())
                .queueCapacity(executor.getQueueCapacity())
                .queueSize(executor.getQueueSize())
                .queueRemainingCapacity(executor.getQueueRemainingCapacity())
                .taskCount(executor.getTaskCount())
                .completedTaskCount(executor.getCompletedTaskCount())
                .waitTaskCount(executor.getQueueSize())
                .build();
    }

}
