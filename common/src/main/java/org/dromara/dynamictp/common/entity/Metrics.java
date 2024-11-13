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

/**
 * Metrics related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Data
public class Metrics {

    /**
     * 正在执行任务的活跃线程大致总数
     */
    private int activeCount;

    /**
     * 大致任务总数
     */
    private long taskCount;

    /**
     * 执行超时任务数量
     */
    private long runTimeoutCount;

    /**
     * 是否为DtpExecutor
     */
    private boolean dynamic;

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
