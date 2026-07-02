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
import org.dromara.dynamictp.common.entity.AlarmInfo;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.ex.DtpException;
import org.dromara.dynamictp.core.notifier.alarm.AlarmCounter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * AlarmCounter test
 *
 * @author yanhom
 * @since 1.2.2
 */
class AlarmCounterTest {

    // ==================== AlarmCounter ====================

    @Test
    void testInitAndIncAlarmCount() {
        NotifyItem item = new NotifyItem();
        item.setType(NotifyItemEnum.REJECT.getValue());
        item.setPeriod(120);

        String poolName = "test-init-pool";
        AlarmCounter.initAlarmCounter(poolName, item);

        // first increment creates AlarmInfo internally
        AlarmCounter.incAlarmCount(poolName, NotifyItemEnum.REJECT.getValue());
        AlarmCounter.incAlarmCount(poolName, NotifyItemEnum.REJECT.getValue());

        AlarmInfo info = AlarmCounter.getAlarmInfo(poolName, NotifyItemEnum.REJECT.getValue());
        assertNotNull(info);
        assertEquals(2, info.getCount());
    }

    @Test
    void testInitAlarmCounterSkipsChangeType() {
        NotifyItem item = new NotifyItem();
        item.setType(NotifyItemEnum.CHANGE.getValue());

        String poolName = "test-skip-pool";
        AlarmCounter.initAlarmCounter(poolName, item);

        // CHANGE type should not be initialized, getAlarmInfo should throw
        assertThrows(DtpException.class,
                () -> AlarmCounter.getAlarmInfo(poolName, NotifyItemEnum.CHANGE.getValue()));
    }

    @Test
    void testGetAlarmInfoThrowsWhenNotInitialized() {
        assertThrows(DtpException.class,
                () -> AlarmCounter.getAlarmInfo("nonexistent-pool", NotifyItemEnum.REJECT.getValue()));
    }

    @Test
    void testGetAlarmInfoReturnsNullWhenCacheMiss() {
        NotifyItem item = new NotifyItem();
        item.setType(NotifyItemEnum.LIVENESS.getValue());
        item.setPeriod(120);

        String poolName = "test-miss-pool";
        AlarmCounter.initAlarmCounter(poolName, item);

        // initialized but no count yet => cache miss => null
        AlarmInfo info = AlarmCounter.getAlarmInfo(poolName, NotifyItemEnum.LIVENESS.getValue());
        assertNull(info);
    }

    @Test
    void testResetClearsCounterAndRecordsTime() {
        NotifyItem item = new NotifyItem();
        item.setType(NotifyItemEnum.CAPACITY.getValue());
        item.setPeriod(120);

        String poolName = "test-reset-pool";
        AlarmCounter.initAlarmCounter(poolName, item);
        AlarmCounter.incAlarmCount(poolName, NotifyItemEnum.CAPACITY.getValue());
        AlarmCounter.incAlarmCount(poolName, NotifyItemEnum.CAPACITY.getValue());
        AlarmCounter.incAlarmCount(poolName, NotifyItemEnum.CAPACITY.getValue());

        assertEquals(3, AlarmCounter.getAlarmInfo(poolName, NotifyItemEnum.CAPACITY.getValue()).getCount());

        AlarmCounter.reset(poolName, NotifyItemEnum.CAPACITY.getValue());

        assertEquals(0, AlarmCounter.getAlarmInfo(poolName, NotifyItemEnum.CAPACITY.getValue()).getCount());
        assertNotNull(AlarmCounter.getLastAlarmTime(poolName, NotifyItemEnum.CAPACITY.getValue()));
    }

    @Test
    void testGetLastAlarmTimeBeforeReset() {
        assertNull(AlarmCounter.getLastAlarmTime("no-time-pool", NotifyItemEnum.REJECT.getValue()));
    }
}
