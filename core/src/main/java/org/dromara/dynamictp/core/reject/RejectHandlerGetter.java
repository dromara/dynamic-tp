package org.dromara.dynamictp.core.reject;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.ex.DtpException;

import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import static org.dromara.dynamictp.common.em.RejectedTypeEnum.ABORT_POLICY;
import static org.dromara.dynamictp.common.em.RejectedTypeEnum.CALLER_RUNS_POLICY;
import static org.dromara.dynamictp.common.em.RejectedTypeEnum.DISCARD_OLDEST_POLICY;
import static org.dromara.dynamictp.common.em.RejectedTypeEnum.DISCARD_POLICY;

/**
 * RejectHandlerGetter related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class RejectHandlerGetter {

    private RejectHandlerGetter() { }

    public static RejectedExecutionHandler buildRejectedHandler(String name) {
        if (Objects.equals(name, ABORT_POLICY.getName())) {
            return new ThreadPoolExecutor.AbortPolicy();
        } else if (Objects.equals(name, CALLER_RUNS_POLICY.getName())) {
            return new ThreadPoolExecutor.CallerRunsPolicy();
        } else if (Objects.equals(name, DISCARD_OLDEST_POLICY.getName())) {
            return new ThreadPoolExecutor.DiscardOldestPolicy();
        } else if (Objects.equals(name, DISCARD_POLICY.getName())) {
            return new ThreadPoolExecutor.DiscardPolicy();
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

    public static RejectedExecutionHandler getProxy(String name) {
        return getProxy(buildRejectedHandler(name));
    }

    public static RejectedExecutionHandler getProxy(RejectedExecutionHandler handler) {
        return (RejectedExecutionHandler) Proxy
                .newProxyInstance(handler.getClass().getClassLoader(),
                        new Class[]{RejectedExecutionHandler.class},
                        new RejectedInvocationHandler(handler));
    }
}
