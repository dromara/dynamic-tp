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

import org.dromara.dynamictp.core.support.DynamicTp;
import org.dromara.dynamictp.core.support.ThreadPoolBuilder;
import org.dromara.dynamictp.core.support.ThreadPoolCreator;
import org.dromara.dynamictp.core.thread.DtpExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.dromara.dynamictp.common.em.QueueTypeEnum.SYNCHRONOUS_QUEUE;

/**
 * @author Redick01
 */
@Configuration
public class ThreadPoolConfiguration {

    /**
     * 通过{@link DynamicTp} 注解定义普通juc线程池，会享受到该框架监控功能，注解名称优先级高于方法名
     *
     * @return 线程池实例
     */
//    @DynamicTp("commonExecutor")
//    @Bean
//    public ThreadPoolExecutor commonExecutor() {
//        return (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
//    }

    /**
     * 通过{@link ThreadPoolCreator} 快速创建一些简单配置的动态线程池
     * tips: 建议直接在配置中心配置就行，不用@Bean声明
     *
     * @return 线程池实例
     */
    @Bean
    public DtpExecutor dtpExecutor1() {
        return ThreadPoolCreator.createDynamicFast("dtpExecutor1");
    }

    /**
     * 通过{@link ThreadPoolBuilder} 设置详细参数创建动态线程池（推荐方式），
     * ioIntensive，参考tomcat线程池设计，实现了处理io密集型任务的线程池，具体参数可以看代码注释
     *
     * tips: 建议直接在配置中心配置就行，不用@Bean声明
     * @return 线程池实例
     */
//    @Bean
//    public DtpExecutor ioIntensiveExecutor() {
//        return ThreadPoolBuilder.newBuilder()
//                .threadPoolName("ioIntensiveExecutor")
//                .corePoolSize(20)
//                .maximumPoolSize(50)
//                .queueCapacity(2048)
//                .ioIntensive(true)
//                .buildDynamic();
//    }

    /**
     * tips: 建议直接在配置中心配置就行，不用@Bean声明
     * @return 线程池实例
     */
//    @Bean
//    public ThreadPoolExecutor dtpExecutor2() {
//        return ThreadPoolBuilder.newBuilder()
//                .threadPoolName("dtpExecutor2")
//                .corePoolSize(10)
//                .maximumPoolSize(15)
//                .keepAliveTime(15000)
//                .timeUnit(TimeUnit.MILLISECONDS)
//                .workQueue(SYNCHRONOUS_QUEUE.getName(), null, false, null)
//                .waitForTasksToCompleteOnShutdown(true)
//                .awaitTerminationSeconds(5)
//                .buildDynamic();
//    }
}
