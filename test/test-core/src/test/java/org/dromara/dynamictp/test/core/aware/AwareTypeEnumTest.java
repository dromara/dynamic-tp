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

package org.dromara.dynamictp.test.core.aware;

import org.dromara.dynamictp.core.aware.AwareTypeEnum;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * AwareTypeEnumTest related.
 */
class AwareTypeEnumTest {

    @Test
    void testAwareTypesKeepExpectedOrder() {
        AwareTypeEnum[] values = AwareTypeEnum.values();

        assertArrayEquals(new AwareTypeEnum[] {
                AwareTypeEnum.PERFORMANCE_MONITOR_AWARE,
                AwareTypeEnum.TASK_TIMEOUT_AWARE,
                AwareTypeEnum.TASK_REJECT_AWARE
        }, values);
        assertArrayEquals(new int[] {1, 2, 3}, Arrays.stream(values).mapToInt(AwareTypeEnum::getOrder).toArray());
    }

    @Test
    void testAwareTypeNames() {
        assertEquals("monitor", AwareTypeEnum.PERFORMANCE_MONITOR_AWARE.getName());
        assertEquals("timeout", AwareTypeEnum.TASK_TIMEOUT_AWARE.getName());
        assertEquals("reject", AwareTypeEnum.TASK_REJECT_AWARE.getName());
    }
}
