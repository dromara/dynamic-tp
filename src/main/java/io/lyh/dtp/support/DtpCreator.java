package io.lyh.dtp.support;

import io.lyh.dtp.core.DtpExecutor;
import io.lyh.dtp.core.ThreadPoolBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * DtpCreator related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class DtpCreator {

    public static ExecutorService createWithTtl(boolean dynamic, String threadPrefix) {
        return createWithTtl(null, dynamic, threadPrefix);
    }

    public static ExecutorService createWithTtl(String name, boolean dynamic, String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName(name)
                .dynamic(dynamic)
                .threadFactory(threadPrefix)
                .buildWrapperWithTtl();
    }

    public static ThreadPoolExecutor createCommonFast(String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .threadFactory(threadPrefix)
                .buildCommon();
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
}
