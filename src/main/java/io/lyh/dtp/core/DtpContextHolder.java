package io.lyh.dtp.core;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * DtpContextHolder, wraps ThreadLocal with ttl.
 *
 * @author: yanhom1314@gmail.com
 * @date 2022-01-03 上午1:56
 */
public class DtpContextHolder {

    private static final TransmittableThreadLocal<DtpContext> CONTEXT = new TransmittableThreadLocal();

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
