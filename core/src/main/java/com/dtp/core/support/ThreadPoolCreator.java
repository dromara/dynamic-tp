package com.dtp.core.support;

import com.dtp.common.em.QueueTypeEnum;
import com.dtp.core.thread.DtpExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Offer a fast dtp creator, use only in simple scenario.
 * It is best to use ThreadPoolBuilder and assign relevant values.
 *
 * @author yanhom
 * @since 1.0.0
 **/
public class ThreadPoolCreator {

    private ThreadPoolCreator() { }

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

    public static DtpExecutor createDynamicFast(String poolName) {
        return createDynamicFast(poolName, poolName);
    }

    public static DtpExecutor createDynamicFast(String poolName, String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName(poolName)
                .threadFactory(threadPrefix)
                .buildDynamic();
    }

    public static ExecutorService createDynamicWithTtl(String poolName) {
        return createDynamicWithTtl(poolName, poolName);
    }

    public static ExecutorService createDynamicWithTtl(String poolName, String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName(poolName)
                .threadFactory(threadPrefix)
                .buildWithTtl();
    }

    public static ThreadPoolExecutor newSingleThreadPool(String threadPrefix, int queueCapacity) {
        return newFixedThreadPool(threadPrefix, 1, queueCapacity);
    }

    public static ThreadPoolExecutor newFixedThreadPool(String threadPrefix, int poolSize, int queueCapacity) {
        return ThreadPoolBuilder.newBuilder()
                .corePoolSize(poolSize)
                .maximumPoolSize(poolSize)
                .workQueue(QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE.getName(), queueCapacity, null)
                .threadFactory(threadPrefix)
                .buildDynamic();
    }

    public static ExecutorService newCachedThreadPool(String threadPrefix, int maximumPoolSize) {
        return ThreadPoolBuilder.newBuilder()
                .corePoolSize(0)
                .maximumPoolSize(maximumPoolSize)
                .workQueue(QueueTypeEnum.SYNCHRONOUS_QUEUE.getName(), null, null)
                .threadFactory(threadPrefix)
                .buildDynamic();
    }

    public static ThreadPoolExecutor newThreadPool(String threadPrefix, int corePoolSize,
                                                   int maximumPoolSize, int queueCapacity) {
        return ThreadPoolBuilder.newBuilder()
                .corePoolSize(corePoolSize)
                .maximumPoolSize(maximumPoolSize)
                .workQueue(QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE.getName(), queueCapacity, null)
                .threadFactory(threadPrefix)
                .buildDynamic();
    }

    /**
     * 阻塞系数 = 阻塞时间／（阻塞时间+使用CPU的时间）
     * 建议线程数 = CPU可用核心数 / (1 - 阻塞系数)
     * 计算密集型任务的阻塞系数为0，而IO密集型任务的阻塞系数则接近于1
     *
     * @param blockingCoefficient 阻塞系数，阻塞因子介于0~1之间的数，阻塞因子越大，线程池中的线程数越多
     * @return {@link ThreadPoolExecutor}
     */
    public static ThreadPoolExecutor newExecutorByBlockingCoefficient(float blockingCoefficient) {
        if (blockingCoefficient >= 1 || blockingCoefficient < 0) {
            throw new IllegalArgumentException();
        }

        int poolSize = (int) (Runtime.getRuntime().availableProcessors() / (1 - blockingCoefficient));
        return ThreadPoolBuilder.newBuilder()
                .corePoolSize(poolSize)
                .maximumPoolSize(poolSize)
                .buildDynamic();
    }
}
