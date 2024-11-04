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

package org.dromara.dynamictp.example.config;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.executor.OrderedDtpExecutor;
import org.dromara.dynamictp.core.support.DynamicTp;
import org.dromara.dynamictp.core.support.ThreadPoolBuilder;
import org.dromara.dynamictp.core.support.ThreadPoolCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.dromara.dynamictp.common.em.QueueTypeEnum.MEMORY_SAFE_LINKED_BLOCKING_QUEUE;
import static org.dromara.dynamictp.common.em.RejectedTypeEnum.CALLER_RUNS_POLICY;

/**
 * @author Redick01
 */
@Slf4j
@Configuration
public class ThreadPoolConfiguration {

    /**
     * 通过{@link DynamicTp} 注解定义普通juc线程池，会享受到该框架增强能力，注解名称优先级高于方法名
     *
     * @return 线程池实例
     */
    @DynamicTp("jucThreadPoolExecutor")
    @Bean
    public ThreadPoolExecutor jucThreadPoolExecutor() {
        return (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    }

    /**
     * 通过{@link DynamicTp} 注解定义spring线程池，会享受到该框架增强能力，注解名称优先级高于方法名
     *
     * @return 线程池实例
     */
    @DynamicTp("threadPoolTaskExecutor")
    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setTaskDecorator(r -> () -> {
            log.info("before execute");
            r.run();
            log.info("after execute");
        });
        return executor;
    }

    /**
     * 通过{@link ThreadPoolCreator} 快速创建一些简单配置的线程池，使用默认参数
     * tips: 建议直接在配置中心配置就行，不用@Bean声明
     *
     * @return 线程池实例
     */
    @Bean
    public DtpExecutor dtpExecutor0() {
        return ThreadPoolCreator.createDynamicFast("dtpExecutor0");
    }

    /**
     * 通过{@link ThreadPoolBuilder} 设置详细参数创建动态线程池
     * tips: 建议直接在配置中心配置就行，不用@Bean声明
     * @return 线程池实例
     */
    @Bean
    public ThreadPoolExecutor dtpExecutor1() {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName("dtpExecutor1")
                .threadFactory("test-dtp-common")
                .corePoolSize(10)
                .maximumPoolSize(15)
                .keepAliveTime(40)
                .timeUnit(TimeUnit.SECONDS)
                .workQueue(MEMORY_SAFE_LINKED_BLOCKING_QUEUE.getName(), 2000)
                .buildDynamic();
    }

    /**
     * 通过{@link ThreadPoolBuilder} 设置详细参数创建动态线程池
     * eager，参考tomcat线程池设计，适用于处理io密集型任务场景，具体参数可以看代码注释
     * tips: 建议直接在配置中心配置就行，不用@Bean声明
     * @return 线程池实例
     */
    @Bean
    public DtpExecutor eagerDtpExecutor() {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName("eagerDtpExecutor")
                .threadFactory("test-eager")
                .corePoolSize(2)
                .maximumPoolSize(4)
                .queueCapacity(2000)
                .eager()
                .buildDynamic();
    }

    /**
     * 通过{@link ThreadPoolBuilder} 设置详细参数创建动态线程池
     * ordered，适用于处理有序任务场景，任务要实现Ordered接口，具体参数可以看代码注释
     * tips: 建议直接在配置中心配置就行，不用@Bean声明
     * @return 线程池实例
     */
    @Bean
    public OrderedDtpExecutor orderedDtpExecutor() {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName("orderedDtpExecutor")
                .threadFactory("test-ordered")
                .corePoolSize(4)
                .maximumPoolSize(4)
                .queueCapacity(2000)
                .buildOrdered();
    }

    /**
     * 通过{@link ThreadPoolBuilder} 设置详细参数创建线程池
     * scheduled，适用于处理定时任务场景，具体参数可以看代码注释
     * tips: 建议直接在配置中心配置就行，不用@Bean声明
     * @return 线程池实例
     */
    @Bean
    public ScheduledExecutorService scheduledDtpExecutor() {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName("scheduledDtpExecutor")
                .corePoolSize(2)
                .dynamic(true)
                .threadFactory("test-scheduled")
                .rejectedExecutionHandler(CALLER_RUNS_POLICY.getName())
                .buildScheduled();
    }
}
