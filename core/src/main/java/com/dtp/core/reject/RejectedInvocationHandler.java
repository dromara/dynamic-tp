package com.dtp.core.reject;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * RejectedInvocationHandler related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public class RejectedInvocationHandler implements InvocationHandler, RejectedAware {

    private final Object target;

    public RejectedInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            Runnable runnable = (Runnable) args[0];
            ThreadPoolExecutor executor = (ThreadPoolExecutor) args[1];
            beforeReject(runnable, executor, log);
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
}
