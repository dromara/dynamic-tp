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

import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.AlarmInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * AlarmInfoTest related.
 */
class AlarmInfoTest {

    @Test
    void testCounterCanIncrementAndReset() {
        AlarmInfo alarmInfo = new AlarmInfo();

        alarmInfo.incCounter();
        alarmInfo.incCounter();

        Assertions.assertEquals(2, alarmInfo.getCount());

        alarmInfo.reset();

        Assertions.assertEquals(0, alarmInfo.getCount());
    }

    @Test
    void testChainSetterReturnsSameInstance() {
        AlarmInfo alarmInfo = new AlarmInfo();

        AlarmInfo result = alarmInfo.setNotifyItem(NotifyItemEnum.REJECT);

        Assertions.assertSame(alarmInfo, result);
        Assertions.assertEquals(NotifyItemEnum.REJECT, alarmInfo.getNotifyItem());
    }
}
