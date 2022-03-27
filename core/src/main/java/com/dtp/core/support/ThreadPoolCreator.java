package com.dtp.core.support;

import com.dtp.core.thread.DtpExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Offer a fast dtp creator, use only in simple scenario.
 * It is best to use ThreadPoolBuilder and assign relevant values.
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class ThreadPoolCreator {

    private ThreadPoolCreator() {}

    public static ThreadPoolExecutor createCommonFast(String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .threadFactory(threadPrefix)
                .buildCommon();
    }

    public static ExecutorService createCommonWithTtl(String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .dynamic(false)
                .threadFactory(threadPrefix)
                .buildWithTtl();
    }

    public static DtpExecutor createDynamicFast(String name) {
        return createDynamicFast(name, name);
    }

    public static DtpExecutor createDynamicFast(String name, String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName(name)
                .threadFactory(threadPrefix)
                .buildDynamic();
    }

    public static ExecutorService createDynamicWithTtl(String name) {
        return createDynamicWithTtl(name, name);
    }

    public static ExecutorService createDynamicWithTtl(String name, String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName(name)
                .threadFactory(threadPrefix)
                .buildWithTtl();
    }
}
