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

import org.dromara.dynamictp.common.em.RejectedTypeEnum;
import org.dromara.dynamictp.common.entity.TpExecutorProps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

/**
 * TpExecutorPropsTest related.
 */
class TpExecutorPropsTest {

    @Test
    void testDefaultsAreValid() {
        TpExecutorProps props = new TpExecutorProps();

        Assertions.assertFalse(props.coreParamIsInValid());
        Assertions.assertEquals("dtp", props.getThreadNamePrefix());
        Assertions.assertEquals(1, props.getCorePoolSize());
        Assertions.assertEquals(60, props.getKeepAliveTime());
        Assertions.assertEquals(TimeUnit.SECONDS, props.getUnit());
        Assertions.assertEquals(1024, props.getQueueCapacity());
        Assertions.assertEquals(16, props.getMaxFreeMemory());
        Assertions.assertEquals(RejectedTypeEnum.ABORT_POLICY.getName(), props.getRejectedHandlerType());
        Assertions.assertTrue(props.isRejectEnhanced());
        Assertions.assertTrue(props.isNotifyEnabled());
        Assertions.assertTrue(props.isWaitForTasksToCompleteOnShutdown());
        Assertions.assertEquals(3, props.getAwaitTerminationSeconds());
    }

    @Test
    void testCoreParamIsInvalidWhenCorePoolSizeIsNegative() {
        TpExecutorProps props = new TpExecutorProps();
        props.setCorePoolSize(-1);

        Assertions.assertTrue(props.coreParamIsInValid());
    }

    @Test
    void testCoreParamIsInvalidWhenMaximumPoolSizeIsNotPositive() {
        TpExecutorProps props = new TpExecutorProps();
        props.setMaximumPoolSize(0);

        Assertions.assertTrue(props.coreParamIsInValid());
    }

    @Test
    void testCoreParamIsInvalidWhenMaximumPoolSizeLessThanCorePoolSize() {
        TpExecutorProps props = new TpExecutorProps();
        props.setCorePoolSize(2);
        props.setMaximumPoolSize(1);

        Assertions.assertTrue(props.coreParamIsInValid());
    }

    @Test
    void testCoreParamIsInvalidWhenKeepAliveTimeIsNegative() {
        TpExecutorProps props = new TpExecutorProps();
        props.setKeepAliveTime(-1);

        Assertions.assertTrue(props.coreParamIsInValid());
    }
}
