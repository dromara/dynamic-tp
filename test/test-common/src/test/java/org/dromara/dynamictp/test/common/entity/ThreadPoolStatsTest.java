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

package org.dromara.dynamictp.test.common.entity;

import org.dromara.dynamictp.common.entity.Metrics;
import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * ThreadPoolStatsTest related.
 */
class ThreadPoolStatsTest {

    @Test
    void testThreadPoolStatsCanBeSetAndRead() {
        ThreadPoolStats stats = new ThreadPoolStats();

        stats.setPoolName("dtpExecutor");
        stats.setPoolAliasName("alias");
        stats.setCorePoolSize(2);
        stats.setMaximumPoolSize(4);
        stats.setKeepAliveTime(60L);
        stats.setQueueType("VariableLinkedBlockingQueue");
        stats.setQueueCapacity(100);
        stats.setQueueSize(10);
        stats.setFair(true);
        stats.setQueueRemainingCapacity(90);
        stats.setActiveCount(1);
        stats.setTaskCount(20L);
        stats.setCompletedTaskCount(18L);
        stats.setLargestPoolSize(3);
        stats.setPoolSize(2);
        stats.setWaitTaskCount(5);
        stats.setRejectCount(1L);
        stats.setRejectHandlerName("AbortPolicy");
        stats.setDynamic(true);
        stats.setRunTimeoutCount(2L);
        stats.setQueueTimeoutCount(3L);
        stats.setTps(12.5D);
        stats.setMaxRt(100L);
        stats.setMinRt(10L);
        stats.setAvg(30.5D);
        stats.setTp50(20.0D);
        stats.setTp75(35.0D);
        stats.setTp90(50.0D);
        stats.setTp95(75.0D);
        stats.setTp99(95.0D);
        stats.setTp999(99.9D);

        Assertions.assertTrue(stats instanceof Metrics);
        Assertions.assertEquals("dtpExecutor", stats.getPoolName());
        Assertions.assertEquals("alias", stats.getPoolAliasName());
        Assertions.assertEquals(2, stats.getCorePoolSize());
        Assertions.assertEquals(4, stats.getMaximumPoolSize());
        Assertions.assertEquals(60L, stats.getKeepAliveTime());
        Assertions.assertEquals("VariableLinkedBlockingQueue", stats.getQueueType());
        Assertions.assertEquals(100, stats.getQueueCapacity());
        Assertions.assertEquals(10, stats.getQueueSize());
        Assertions.assertTrue(stats.isFair());
        Assertions.assertEquals(90, stats.getQueueRemainingCapacity());
        Assertions.assertEquals(1, stats.getActiveCount());
        Assertions.assertEquals(20L, stats.getTaskCount());
        Assertions.assertEquals(18L, stats.getCompletedTaskCount());
        Assertions.assertEquals(3, stats.getLargestPoolSize());
        Assertions.assertEquals(2, stats.getPoolSize());
        Assertions.assertEquals(5, stats.getWaitTaskCount());
        Assertions.assertEquals(1L, stats.getRejectCount());
        Assertions.assertEquals("AbortPolicy", stats.getRejectHandlerName());
        Assertions.assertTrue(stats.isDynamic());
        Assertions.assertEquals(2L, stats.getRunTimeoutCount());
        Assertions.assertEquals(3L, stats.getQueueTimeoutCount());
        Assertions.assertEquals(12.5D, stats.getTps());
        Assertions.assertEquals(100L, stats.getMaxRt());
        Assertions.assertEquals(10L, stats.getMinRt());
        Assertions.assertEquals(30.5D, stats.getAvg());
        Assertions.assertEquals(20.0D, stats.getTp50());
        Assertions.assertEquals(35.0D, stats.getTp75());
        Assertions.assertEquals(50.0D, stats.getTp90());
        Assertions.assertEquals(75.0D, stats.getTp95());
        Assertions.assertEquals(95.0D, stats.getTp99());
        Assertions.assertEquals(99.9D, stats.getTp999());
        Assertions.assertTrue(stats.toString().contains("poolName=dtpExecutor"));
    }
}
