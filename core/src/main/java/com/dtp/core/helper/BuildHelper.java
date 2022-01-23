package com.dtp.core.helper;

import com.dtp.common.dto.DtpMainProp;
import com.dtp.common.ex.DtpException;
import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.reject.RejectedCountableAbortPolicy;
import com.dtp.core.reject.RejectedCountableCallerRunsPolicy;
import com.dtp.core.reject.RejectedCountableDiscardOldestPolicy;
import com.dtp.core.reject.RejectedCountableDiscardPolicy;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;

import static com.dtp.common.em.RejectedTypeEnum.*;

/**
 * BuildHelper related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
public class BuildHelper {

    private BuildHelper() {}

    public static RejectedExecutionHandler buildRejectedHandler(String name) {
        if (Objects.equals(name, ABORT_POLICY.getName())) {
            return new RejectedCountableAbortPolicy();
        } else if (Objects.equals(name, CALLER_RUNS_POLICY.getName())) {
            return new RejectedCountableCallerRunsPolicy();
        } else if (Objects.equals(name, DISCARD_OLDEST_POLICY.getName())) {
            return new RejectedCountableDiscardOldestPolicy();
        } else if (Objects.equals(name, DISCARD_POLICY.getName())) {
            return new RejectedCountableDiscardPolicy();
        }

        ServiceLoader<RejectedExecutionHandler> serviceLoader = ServiceLoader.load(RejectedExecutionHandler.class);
        for (RejectedExecutionHandler handler : serviceLoader) {
            String handlerName = handler.getClass().getSimpleName();
            if (name.equalsIgnoreCase(handlerName)) {
                return handler;
            }
        }

        log.error("Cannot find specified rejectedHandler {}", name);
        throw new DtpException("Cannot find specified rejectedHandler " + name);
    }

    public static DtpMainProp of(DtpExecutor dtpExecutor) {
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
