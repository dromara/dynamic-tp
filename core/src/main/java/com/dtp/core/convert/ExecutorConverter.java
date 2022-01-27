package com.dtp.core.convert;

import com.dtp.common.dto.DtpMainProp;
import com.dtp.core.thread.DtpExecutor;

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
        DtpMainProp wrapper = new DtpMainProp();
        wrapper.setDtpName(dtpExecutor.getThreadPoolName());
        wrapper.setCorePoolSize(dtpExecutor.getCorePoolSize());
        wrapper.setMaxPoolSize(dtpExecutor.getMaximumPoolSize());
        wrapper.setKeepAliveTime(dtpExecutor.getKeepAliveTime(TimeUnit.SECONDS));
        wrapper.setQueueType(dtpExecutor.getQueueName());
        wrapper.setQueueCapacity(dtpExecutor.getQueueCapacity());
        wrapper.setRejectType(dtpExecutor.getRejectHandlerName());
        wrapper.setAllowCoreThreadTimeOut(dtpExecutor.allowsCoreThreadTimeOut());
        return wrapper;
    }
}
