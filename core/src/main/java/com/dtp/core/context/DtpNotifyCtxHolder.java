package com.dtp.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * DtpNotifyCtxHolder, wraps ThreadLocal with ttl.
 *
 * @author: yanhom
 * @since 1.0.0
 */
public class DtpNotifyCtxHolder {

    private static final TransmittableThreadLocal<BaseNotifyCtx> CONTEXT = new TransmittableThreadLocal<>();

    private DtpNotifyCtxHolder() {}

    public static void set(BaseNotifyCtx dtpContext) {
        CONTEXT.set(dtpContext);
    }

    public static BaseNotifyCtx get() {
        return CONTEXT.get();
    }

    public static void remove() {
        CONTEXT.remove();
    }

}
