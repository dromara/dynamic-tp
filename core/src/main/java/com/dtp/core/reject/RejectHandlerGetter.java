package com.dtp.core.reject;

import com.dtp.common.ex.DtpException;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.RejectedExecutionHandler;

import static com.dtp.common.em.RejectedTypeEnum.*;
import static com.dtp.common.em.RejectedTypeEnum.DISCARD_POLICY;

/**
 * RejectHandlerGetter related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
public class RejectHandlerGetter {

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
}
