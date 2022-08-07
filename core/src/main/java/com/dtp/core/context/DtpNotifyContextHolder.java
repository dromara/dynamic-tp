package com.dtp.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * DtpNotifyContextHolder, wraps ThreadLocal with ttl.
 *
 * @author: yanhom
 * @since 1.0.0
 */
public class DtpNotifyContextHolder {

    private static final TransmittableThreadLocal<DtpNotifyContext> CONTEXT = new TransmittableThreadLocal<>();

    private DtpNotifyContextHolder() {}

    public static void set(DtpNotifyContext dtpContext) {
        CONTEXT.set(dtpContext);
    }

    public static DtpNotifyContext get() {
        return CONTEXT.get();
    }

    public static void remove() {
        CONTEXT.remove();
    }

}
