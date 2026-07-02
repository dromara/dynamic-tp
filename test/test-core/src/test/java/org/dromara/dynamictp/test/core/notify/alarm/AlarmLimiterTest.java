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

package org.dromara.dynamictp.test.core.notify.alarm;

import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.core.notifier.alarm.AlarmLimiter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AlarmLimiter test.
 *
 * @author yanhom
 * @since 1.2.2
 */
class AlarmLimiterTest {

    @Test
    void testInitAlarmLimiterSkipsChangeType() {
        NotifyItem item = new NotifyItem();
        item.setType(NotifyItemEnum.CHANGE.getValue());
        item.setSilencePeriod(60);

        String poolName = "limiter-skip-pool";
        AlarmLimiter.initAlarmLimiter(poolName, item);

        assertNull(AlarmLimiter.getAlarmLimitInfo(
                poolName + "#" + NotifyItemEnum.CHANGE.getValue(), NotifyItemEnum.CHANGE.getValue()));
    }

    @Test
    void testIsAllowedBeforePut() {
        NotifyItem item = new NotifyItem();
        item.setType(NotifyItemEnum.REJECT.getValue());
        item.setSilencePeriod(60);

        String poolName = "limiter-allowed-pool";
        AlarmLimiter.initAlarmLimiter(poolName, item);

        assertTrue(AlarmLimiter.isAllowed(poolName, NotifyItemEnum.REJECT.getValue()));
    }

    @Test
    void testIsNotAllowedAfterPut() {
        NotifyItem item = new NotifyItem();
        item.setType(NotifyItemEnum.REJECT.getValue());
        item.setSilencePeriod(60);

        String poolName = "limiter-blocked-pool";
        AlarmLimiter.initAlarmLimiter(poolName, item);
        AlarmLimiter.putVal(poolName, NotifyItemEnum.REJECT.getValue());

        assertFalse(AlarmLimiter.isAllowed(poolName, NotifyItemEnum.REJECT.getValue()));
    }

    @Test
    void testGetAlarmLimitInfoAfterPut() {
        NotifyItem item = new NotifyItem();
        item.setType(NotifyItemEnum.RUN_TIMEOUT.getValue());
        item.setSilencePeriod(60);

        String poolName = "limiter-info-pool";
        AlarmLimiter.initAlarmLimiter(poolName, item);
        AlarmLimiter.putVal(poolName, NotifyItemEnum.RUN_TIMEOUT.getValue());

        String key = poolName + "#" + NotifyItemEnum.RUN_TIMEOUT.getValue();
        String val = AlarmLimiter.getAlarmLimitInfo(key, NotifyItemEnum.RUN_TIMEOUT.getValue());
        assertNotNull(val);
        assertEquals(NotifyItemEnum.RUN_TIMEOUT.getValue(), val);
    }

    @Test
    void testGetAlarmLimitInfoReturnsNullForNonexistentKey() {
        assertNull(AlarmLimiter.getAlarmLimitInfo("nonexistent#reject", "reject"));
    }

    @Test
    void testGenKey() {
        assertEquals("pool#reject", AlarmLimiter.genKey("pool", "reject"));
    }
}
