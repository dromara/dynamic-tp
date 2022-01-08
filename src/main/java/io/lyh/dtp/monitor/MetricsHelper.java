package io.lyh.dtp.monitor;

import io.lyh.dtp.core.DtpExecutor;

/**
 * MetricsHelper related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2022-01-06 16:55
 * @since 1.0.0
 **/
public class MetricsHelper {

    public static ThreadPoolMetrics getMetrics(DtpExecutor dtpExecutor) {
        ThreadPoolMetrics metrics = new ThreadPoolMetrics();
        metrics.setDtpName(dtpExecutor.getThreadPoolName());
        metrics.setCorePoolSize(dtpExecutor.getCorePoolSize());
        metrics.setMaximumPoolSize(dtpExecutor.getMaximumPoolSize());
        metrics.setQueueType(dtpExecutor.getQueueName());
        metrics.setQueueCapacity(dtpExecutor.getQueueCapacity());
        metrics.setQueueSize(dtpExecutor.getQueue().size());
        metrics.setQueueRemainingCapacity(dtpExecutor.getQueue().remainingCapacity());
        metrics.setActiveCount(dtpExecutor.getActiveCount());
        metrics.setTaskCount(dtpExecutor.getTaskCount());
        metrics.setCompletedTaskCount(dtpExecutor.getCompletedTaskCount());
        metrics.setLargestPoolSize(dtpExecutor.getLargestPoolSize());
        metrics.setPoolSize(dtpExecutor.getPoolSize());
        metrics.setWaitTaskCount(dtpExecutor.getQueue().size());
        metrics.setRejectHandlerName(dtpExecutor.getRejectHandlerName());
        metrics.setRejectCount(dtpExecutor.getRejectCount());
        return metrics;
    }
}
