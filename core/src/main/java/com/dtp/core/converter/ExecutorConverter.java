package com.dtp.core.converter;

import com.dtp.common.entity.ThreadPoolStats;
import com.dtp.common.entity.TpMainFields;
import com.dtp.core.support.ExecutorAdapter;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.thread.DtpExecutor;
import lombok.val;

import java.util.concurrent.TimeUnit;

/**
 * ExecutorConverter related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public class ExecutorConverter {

    private ExecutorConverter() { }

    public static TpMainFields toMainFields(ExecutorWrapper executorWrapper) {
        TpMainFields mainFields = new TpMainFields();
        mainFields.setThreadPoolName(executorWrapper.getThreadPoolName());
        val executor = executorWrapper.getExecutor();
        mainFields.setCorePoolSize(executor.getCorePoolSize());
        mainFields.setMaxPoolSize(executor.getMaximumPoolSize());
        mainFields.setKeepAliveTime(executor.getKeepAliveTime(TimeUnit.SECONDS));
        mainFields.setQueueType(executor.getQueue().getClass().getSimpleName());
        mainFields.setQueueCapacity(executor.getQueueCapacity());
        mainFields.setAllowCoreThreadTimeOut(executor.allowsCoreThreadTimeOut());
        mainFields.setRejectType(executor.getRejectHandlerType());
        return mainFields;
    }

    public static ThreadPoolStats toMetrics(ExecutorWrapper wrapper) {
        ExecutorAdapter<?> executor = wrapper.getExecutor();
        if (executor == null) {
            return null;
        }
        ThreadPoolStats poolStats = convertCommon(executor);
        poolStats.setPoolName(wrapper.getThreadPoolName());
        if (executor instanceof DtpExecutor) {
            DtpExecutor dtpExecutor = (DtpExecutor) executor;
            poolStats.setRejectHandlerName(dtpExecutor.getRejectHandlerType());
            poolStats.setRejectCount(dtpExecutor.getRejectCount());
            poolStats.setRunTimeoutCount(dtpExecutor.getRunTimeoutCount());
            poolStats.setQueueTimeoutCount(dtpExecutor.getQueueTimeoutCount());
            poolStats.setDynamic(true);
        } else {
            poolStats.setDynamic(false);
        }
        return poolStats;
    }

    private static ThreadPoolStats convertCommon(ExecutorAdapter<?> executor) {
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
