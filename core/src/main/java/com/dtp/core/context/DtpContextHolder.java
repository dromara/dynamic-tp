package com.dtp.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * DtpContextHolder, wraps ThreadLocal with ttl.
 *
 * @author: yanhom
 * @since 1.0.0
 */
public class DtpContextHolder {

    private static final TransmittableThreadLocal<DtpContext> CONTEXT = new TransmittableThreadLocal<>();

    private DtpContextHolder() {}

    public static void set(DtpContext dtpContext) {
        CONTEXT.set(dtpContext);
    }

    public static DtpContext get() {
        return CONTEXT.get();
    }

    public static void remove() {
        CONTEXT.remove();
    }

}
