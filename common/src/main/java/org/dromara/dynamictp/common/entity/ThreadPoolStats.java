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

package org.dromara.dynamictp.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * ThreadPoolStats related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class ThreadPoolStats extends Metrics {

    /**
     * 线程池名字
     */
    private String poolName;

    /**
     * 线程池别名
     */
    private String poolAliasName;

    /**
     * 核心线程数
     */
    private int corePoolSize;

    /**
     * 最大线程数
     */
    private int maximumPoolSize;

    /**
     * 空闲时间 (ms)
     */
    private long keepAliveTime;

    /**
     * 队列类型
     */
    private String queueType;

    /**
     * 队列容量
     */
    private int queueCapacity;

    /**
     * 队列任务数量
     */
    private int queueSize;

    /**
     * SynchronousQueue队列模式
     */
    private boolean fair;

    /**
     * 队列剩余容量
     */
    private int queueRemainingCapacity;

    /**
     * 正在执行任务的活跃线程大致总数
     */
    private int activeCount;

    /**
     * 大致任务总数
     */
    private long taskCount;

    /**
     * 已执行完成的大致任务总数
     */
    private long completedTaskCount;

    /**
     * 池中曾经同时存在的最大线程数量
     */
    private int largestPoolSize;

    /**
     * 当前池中存在的线程总数
     */
    private int poolSize;

    /**
     * 等待执行的任务数量
     */
    private int waitTaskCount;

    /**
     * 拒绝的任务数量
     */
    private long rejectCount;

    /**
     * 拒绝策略名称
     */
    private String rejectHandlerName;

    /**
     * 是否DtpExecutor线程池
     */
    private boolean dynamic;

    /**
     * 执行超时任务数量
     */
    private long runTimeoutCount;

    /**
     * 在队列等待超时任务数量
     */
    private long queueTimeoutCount;

    /**
     * tps
     */
    private double tps;

    /**
     * 最大任务耗时
     */
    private long maxRt;

    /**
     * 最小任务耗时
     */
    private long minRt;

    /**
     * 任务平均耗时(单位:ms)
     */
    private double avg;

    /**
     * 满足50%的任务执行所需的最低耗时
     */
    private double tp50;

    /**
     * 满足75%的任务执行所需的最低耗时
     */
    private double tp75;

    /**
     * 满足90%的任务执行所需的最低耗时
     */
    private double tp90;

    /**
     * 满足95%的任务执行所需的最低耗时
     */
    private double tp95;

    /**
     * 满足99%的任务执行所需的最低耗时
     */
    private double tp99;

    /**
     * 满足99.9%的任务执行所需的最低耗时
     */
    private double tp999;
}
