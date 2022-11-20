package com.dtp.core.context;

/**
 * DtpNotifyCtxHolder related.
 *
 * @author yanhom
 * @since 1.0.0
 */
public class DtpNotifyCtxHolder {

    private static final ThreadLocal<BaseNotifyCtx> CONTEXT = new ThreadLocal<>();

    private DtpNotifyCtxHolder() { }

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
