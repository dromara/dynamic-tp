package com.dtp.core.convert;

import com.dtp.common.dto.DtpMainProp;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.core.thread.DtpExecutor;
import lombok.val;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ExecutorConverter related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class ExecutorConverter {

    private ExecutorConverter() {}

    public static DtpMainProp convert(DtpExecutor dtpExecutor) {
        DtpMainProp mainProp = new DtpMainProp();
        mainProp.setThreadPoolName(dtpExecutor.getThreadPoolName());
        mainProp.setCorePoolSize(dtpExecutor.getCorePoolSize());
        mainProp.setMaxPoolSize(dtpExecutor.getMaximumPoolSize());
        mainProp.setKeepAliveTime(dtpExecutor.getKeepAliveTime(TimeUnit.SECONDS));
        mainProp.setQueueType(dtpExecutor.getQueueName());
        mainProp.setQueueCapacity(dtpExecutor.getQueueCapacity());
        mainProp.setRejectType(dtpExecutor.getRejectHandlerName());
        mainProp.setAllowCoreThreadTimeOut(dtpExecutor.allowsCoreThreadTimeOut());
        return mainProp;
    }

    public static DtpMainProp convert(ExecutorWrapper executorWrapper) {
        DtpMainProp mainProp = new DtpMainProp();
        mainProp.setThreadPoolName(executorWrapper.getThreadPoolName());
        val executor = (ThreadPoolExecutor) executorWrapper.getExecutor();
        mainProp.setCorePoolSize(executor.getCorePoolSize());
        mainProp.setMaxPoolSize(executor.getMaximumPoolSize());
        mainProp.setKeepAliveTime(executor.getKeepAliveTime(TimeUnit.SECONDS));
        mainProp.setQueueType(executor.getQueue().getClass().getSimpleName());
        mainProp.setQueueCapacity(executor.getQueue().size() + executor.getQueue().remainingCapacity());
        mainProp.setRejectType(executor.getRejectedExecutionHandler().getClass().getSimpleName());
        mainProp.setAllowCoreThreadTimeOut(executor.allowsCoreThreadTimeOut());
        return mainProp;
    }

    public static DtpMainProp ofSimple(String name, int corePoolSize, int maxPoolSize, long keepAliveTime) {
        DtpMainProp mainProp = new DtpMainProp();
        mainProp.setThreadPoolName(name);
        mainProp.setCorePoolSize(corePoolSize);
        mainProp.setMaxPoolSize(maxPoolSize);
        mainProp.setKeepAliveTime(keepAliveTime);
        return mainProp;
    }
}
