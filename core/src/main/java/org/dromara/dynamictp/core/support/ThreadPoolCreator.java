/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.core.support;

import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.executor.ScheduledDtpExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static org.dromara.dynamictp.common.em.QueueTypeEnum.SYNCHRONOUS_QUEUE;
import static org.dromara.dynamictp.common.em.QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE;

/**
 * Offer a fast dtp creator, use only in simple scenario.
 * It is best to use ThreadPoolBuilder and assign relevant values.
 *
 * @author yanhom
 * @since 1.0.0
 **/
public class ThreadPoolCreator {

    private ThreadPoolCreator() { }

    /**
     * Create a juc thread pool, use default values.
     *
     * @param threadPrefix thread prefix
     * @return the new thread pool
     */
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

    /**
     * Create a dynamic thread pool, use default values.
     *
     * @param poolName thread pool name
     * @return the new thread pool
     */
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

    public static ThreadPoolExecutor newFixedThreadPool(String threadPrefix,
                                                        int poolSize,
                                                        int queueCapacity) {
        return ThreadPoolBuilder.newBuilder()
                .corePoolSize(poolSize)
                .maximumPoolSize(poolSize)
                .keepAliveTime(0L)
                .workQueue(VARIABLE_LINKED_BLOCKING_QUEUE.getName(), queueCapacity, null)
                .threadFactory(threadPrefix)
                .buildCommon();
    }

    public static ThreadPoolExecutor newThreadPool(String threadPrefix, int corePoolSize,
                                                   int maximumPoolSize, int queueCapacity) {
        return ThreadPoolBuilder.newBuilder()
                .corePoolSize(corePoolSize)
                .maximumPoolSize(maximumPoolSize)
                .workQueue(VARIABLE_LINKED_BLOCKING_QUEUE.getName(), queueCapacity, null)
                .threadFactory(threadPrefix)
                .buildCommon();
    }

    public static ScheduledExecutorService newScheduledThreadPool(String threadPrefix, int corePoolSize) {
        return (ScheduledDtpExecutor) ThreadPoolBuilder.newBuilder()
                .corePoolSize(corePoolSize)
                .threadFactory(threadPrefix)
                .scheduled(true)
                .buildCommon();
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
                .buildCommon();
    }
}
