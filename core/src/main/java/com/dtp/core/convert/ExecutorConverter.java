package com.dtp.core.convert;

import com.dtp.common.entity.TpMainFields;
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

    public static TpMainFields convert(DtpExecutor dtpExecutor) {
        TpMainFields mainFields = new TpMainFields();
        mainFields.setThreadPoolName(dtpExecutor.getThreadPoolName());
        mainFields.setCorePoolSize(dtpExecutor.getCorePoolSize());
        mainFields.setMaxPoolSize(dtpExecutor.getMaximumPoolSize());
        mainFields.setKeepAliveTime(dtpExecutor.getKeepAliveTime(TimeUnit.SECONDS));
        mainFields.setQueueType(dtpExecutor.getQueueName());
        mainFields.setQueueCapacity(dtpExecutor.getQueueCapacity());
        mainFields.setRejectType(dtpExecutor.getRejectHandlerName());
        mainFields.setAllowCoreThreadTimeOut(dtpExecutor.allowsCoreThreadTimeOut());
        return mainFields;
    }

    public static TpMainFields convert(ExecutorWrapper executorWrapper) {
        TpMainFields mainFields = new TpMainFields();
        mainFields.setThreadPoolName(executorWrapper.getThreadPoolName());
        val executor = executorWrapper.getExecutor();
        mainFields.setCorePoolSize(executor.getCorePoolSize());
        mainFields.setMaxPoolSize(executor.getMaximumPoolSize());
        mainFields.setKeepAliveTime(executor.getKeepAliveTime(TimeUnit.SECONDS));
        mainFields.setQueueType(executor.getQueue().getClass().getSimpleName());
        mainFields.setQueueCapacity(executor.getQueue().size() + executor.getQueue().remainingCapacity());
        mainFields.setAllowCoreThreadTimeOut(executor.allowsCoreThreadTimeOut());
        mainFields.setRejectType(executor.getRejectHandlerName());
        return mainFields;
    }

    public static TpMainFields ofSimple(String name, int corePoolSize, int maxPoolSize, long keepAliveTime) {
        TpMainFields mainFields = new TpMainFields();
        mainFields.setThreadPoolName(name);
        mainFields.setCorePoolSize(corePoolSize);
        mainFields.setMaxPoolSize(maxPoolSize);
        mainFields.setKeepAliveTime(keepAliveTime);
        return mainFields;
    }
}
