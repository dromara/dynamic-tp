package io.lyh.dtp.common.em;

import io.lyh.dtp.common.ex.DtpException;
import io.lyh.dtp.handler.reject.RejectedCountableAbortPolicy;
import io.lyh.dtp.handler.reject.RejectedCountableCallerRunsPolicy;
import io.lyh.dtp.handler.reject.RejectedCountableDiscardOldestPolicy;
import io.lyh.dtp.handler.reject.RejectedCountableDiscardPolicy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.RejectedExecutionHandler;

/**
 * RejectedTypeEnum related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-27 10:21
 * @since 1.0.0
 **/
@Slf4j
@Getter
public enum RejectedTypeEnum {

    /**
     * Execution handler while trigger reject policy.
     */
    ABORT_POLICY("AbortPolicy"),

    CALLER_RUNS_POLICY("CallerRunsPolicy"),

    DISCARD_OLDEST_POLICY("DiscardOldestPolicy"),

    DISCARD_POLICY("DiscardPolicy");

    private final String name;

    RejectedTypeEnum(String name) {
        this.name = name;
    }

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
            if (name.equals(handlerName)) {
                return handler;
            }
        }

        log.error("Cannot find specified rejectedHandler {}", name);
        throw new DtpException("Cannot find specified rejectedHandler " + name);
    }

    public static String formatRejectName(String name) {
        if (name.startsWith("RejectedCountable")) {
            return name.replace("RejectedCountable", "");
        }
        return name;
    }
}
