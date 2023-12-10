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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

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
     * <p>
     *     corePoolSize: 1
     *     maximumPoolSize: Runtime.getRuntime().availableProcessors()
     *     keepAliveTime: 60s
     *     workQueue: VARIABLE_LINKED_BLOCKING_QUEUE
     *     queueCapacity: 1024
     *     rejectedExecutionHandler: AbortPolicy
     * </p>
     *
     * @param threadPrefix thread prefix
     * @return the new thread pool
     */
    public static ThreadPoolExecutor createCommonFast(String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .threadFactory(threadPrefix)
                .buildCommon();
    }

    /**
     * Create a juc thread pool, wrap with ttl, use default values.
     *
     * <p>
     *     corePoolSize: 1
     *     maximumPoolSize: Runtime.getRuntime().availableProcessors()
     *     keepAliveTime: 60s
     *     workQueue: VARIABLE_LINKED_BLOCKING_QUEUE
     *     queueCapacity: 1024
     *     rejectedExecutionHandler: AbortPolicy
     * </p>
     *
     * @param threadPrefix thread prefix
     * @return the new thread pool
     */
    public static ExecutorService createCommonWithTtl(String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .dynamic(false)
                .threadFactory(threadPrefix)
                .buildWithTtl();
    }

    /**
     * Create a dynamic thread pool, use default values.
     *
     * <p>
     *     corePoolSize: 1
     *     maximumPoolSize: Runtime.getRuntime().availableProcessors()
     *     keepAliveTime: 60s
     *     workQueue: VARIABLE_LINKED_BLOCKING_QUEUE
     *     queueCapacity: 1024
     *     rejectedExecutionHandler: AbortPolicy
     *     threadName: poolName
     *     threadPrefix: poolName
     * </p>
     *
     * @param poolName thread pool name
     * @return the new thread pool
     */
    public static DtpExecutor createDynamicFast(String poolName) {
        return createDynamicFast(poolName, poolName);
    }

    /**
     * Create a dynamic thread pool, use default values.
     *
     * <p>
     *     corePoolSize: 1
     *     maximumPoolSize: Runtime.getRuntime().availableProcessors()
     *     keepAliveTime: 60s
     *     workQueue: VARIABLE_LINKED_BLOCKING_QUEUE
     *     queueCapacity: 1024
     *     rejectedExecutionHandler: AbortPolicy
     *     threadName: poolName
     *     threadPrefix: threadPrefix
     * </p>
     *
     * @param poolName thread pool name
     * @param threadPrefix thread prefix
     * @return the new thread pool
     */
    public static DtpExecutor createDynamicFast(String poolName, String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName(poolName)
                .threadFactory(threadPrefix)
                .buildDynamic();
    }

    /**
     * Create a dynamic thread pool, wrap with ttl, use default values.
     *
     * <p>
     *     corePoolSize: 1
     *     maximumPoolSize: Runtime.getRuntime().availableProcessors()
     *     keepAliveTime: 60s
     *     workQueue: VARIABLE_LINKED_BLOCKING_QUEUE
     *     queueCapacity: 1024
     *     rejectedExecutionHandler: AbortPolicy
     *     threadName: poolName
     *     threadPrefix: poolName
     * </p>
     *
     * @param poolName thread pool name
     * @return the new thread pool
     */
    public static ExecutorService createDynamicWithTtl(String poolName) {
        return createDynamicWithTtl(poolName, poolName);
    }

    /**
     * Create a dynamic thread pool, wrap with ttl, use default values.
     *
     * <p>
     *     corePoolSize: 1
     *     maximumPoolSize: Runtime.getRuntime().availableProcessors()
     *     keepAliveTime: 60s
     *     workQueue: VARIABLE_LINKED_BLOCKING_QUEUE
     *     queueCapacity: 1024
     *     rejectedExecutionHandler: AbortPolicy
     *     threadName: poolName
     *     threadPrefix: threadPrefix
     * </p>
     *
     * @param poolName thread pool name
     * @param threadPrefix thread prefix
     * @return the new thread pool
     */
    public static ExecutorService createDynamicWithTtl(String poolName, String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName(poolName)
                .threadFactory(threadPrefix)
                .buildWithTtl();
    }

    /**
     * Create a single thread pool with bounded queue capacity.
     *
     * <p>
     *     corePoolSize: 1
     *     maximumPoolSize: 1
     *     keepAliveTime: 60s
     *     workQueue: VARIABLE_LINKED_BLOCKING_QUEUE
     *     queueCapacity: queueCapacity
     *     rejectedExecutionHandler: AbortPolicy
     *     threadPrefix: threadPrefix
     * </p>
     *
     * @param threadPrefix thread prefix
     * @param queueCapacity queue capacity
     * @return the new thread pool
     */
    public static ThreadPoolExecutor newSingleThreadPool(String threadPrefix, int queueCapacity) {
        return newFixedThreadPool(threadPrefix, 1, queueCapacity);
    }

    /**
     * Create a fixed thread pool with bounded queue capacity.
     *
     * <p>
     *     corePoolSize: poolSize
     *     maximumPoolSize: poolSize
     *     keepAliveTime: 60s
     *     workQueue: VARIABLE_LINKED_BLOCKING_QUEUE
     *     queueCapacity: queueCapacity
     *     rejectedExecutionHandler: AbortPolicy
     *     threadPrefix: threadPrefix
     * </p>
     *
     * @param threadPrefix thread prefix
     * @param poolSize pool size
     * @param queueCapacity queue capacity
     * @return the new thread pool
     */
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

    /**
     * Create a memory safe thread pool with bounded queue capacity and bounded pool size.
     *
     * <p>
     *     corePoolSize: corePoolSize
     *     maximumPoolSize: maximumPoolSize
     *     keepAliveTime: 60s
     *     workQueue: VARIABLE_LINKED_BLOCKING_QUEUE
     *     queueCapacity: queueCapacity
     *     rejectedExecutionHandler: AbortPolicy
     *     threadPrefix: threadPrefix
     * </p>
     *
     * @param threadPrefix thread prefix
     * @param corePoolSize core pool size
     * @param maximumPoolSize maximum pool size
     * @param queueCapacity queue capacity
     * @return the new thread pool
     */
    public static ThreadPoolExecutor newThreadPool(String threadPrefix, int corePoolSize,
                                                   int maximumPoolSize, int queueCapacity) {
        return ThreadPoolBuilder.newBuilder()
                .corePoolSize(corePoolSize)
                .maximumPoolSize(maximumPoolSize)
                .workQueue(VARIABLE_LINKED_BLOCKING_QUEUE.getName(), queueCapacity, null)
                .threadFactory(threadPrefix)
                .buildCommon();
    }

    /**
     * Create a scheduled thread pool.
     *
     * <p>
     *     corePoolSize: corePoolSize
     *     keepAliveTime: 0
     *     workQueue: DelayedWorkQueue
     *     rejectedExecutionHandler: AbortPolicy
     *     threadPrefix: threadPrefix
     * </p>
     *
     * @param threadPrefix thread prefix
     * @param corePoolSize core pool size
     * @return the new scheduled thread pool
     */
    public static ScheduledExecutorService newScheduledThreadPool(String threadPrefix, int corePoolSize) {
        return ThreadPoolBuilder.newBuilder()
                .dynamic(false)
                .corePoolSize(corePoolSize)
                .maximumPoolSize(corePoolSize)
                .threadFactory(threadPrefix)
                .buildScheduled();
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
