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

package org.dromara.dynamictp.test.common.em;

import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * NotifyItemEnumTest related.
 */
class NotifyItemEnumTest {

    @Test
    void testOfReturnsMatchingNotifyItem() {
        Assertions.assertEquals(NotifyItemEnum.CHANGE, NotifyItemEnum.of("change"));
        Assertions.assertEquals(NotifyItemEnum.RUN_TIMEOUT, NotifyItemEnum.of("run_timeout"));
        Assertions.assertEquals(NotifyItemEnum.QUEUE_TIMEOUT, NotifyItemEnum.of("queue_timeout"));
    }

    @Test
    void testOfReturnsNullWhenValueIsUnknown() {
        Assertions.assertNull(NotifyItemEnum.of("unknown"));
    }

    @Test
    void testGetValueReturnsConfiguredValue() {
        Assertions.assertEquals("liveness", NotifyItemEnum.LIVENESS.getValue());
        Assertions.assertEquals("capacity", NotifyItemEnum.CAPACITY.getValue());
        Assertions.assertEquals("reject", NotifyItemEnum.REJECT.getValue());
    }
}
