package io.lyh.dtp.core;

import com.alibaba.ttl.TransmittableThreadLocal;
import io.lyh.dtp.domain.DtpContextWrapper;

/**
 * DtpContextHolder related
 *
 * @author: yanhom1314@gmail.com
 * @date 2022-01-03 上午1:56
 */
public class DtpContextHolder {

    private static final TransmittableThreadLocal<DtpContextWrapper> context = new TransmittableThreadLocal();

    private DtpContextHolder() {}

    public static void set(DtpContextWrapper dtpContextWrapper) {
        context.set(dtpContextWrapper);
    }

    public static DtpContextWrapper get() {
        return context.get();
    }

    public static void remove() {
        context.remove();
    }

}
