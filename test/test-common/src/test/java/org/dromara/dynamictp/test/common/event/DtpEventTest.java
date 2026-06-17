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

package org.dromara.dynamictp.test.common.event;

import org.dromara.dynamictp.common.event.AlarmCheckEvent;
import org.dromara.dynamictp.common.event.CollectEvent;
import org.dromara.dynamictp.common.event.CustomContextRefreshedEvent;
import org.dromara.dynamictp.common.event.RefreshEvent;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * DtpEventTest related.
 */
class DtpEventTest {

    @Test
    void testAlarmCheckEventKeepsSourceAndProperties() {
        Object source = new Object();
        DtpProperties properties = DtpProperties.getInstance();

        AlarmCheckEvent event = new AlarmCheckEvent(source, properties);

        Assertions.assertSame(source, event.getSource());
        Assertions.assertSame(properties, event.getDtpProperties());
    }

    @Test
    void testCollectEventKeepsSourceAndProperties() {
        Object source = new Object();
        DtpProperties properties = DtpProperties.getInstance();

        CollectEvent event = new CollectEvent(source, properties);

        Assertions.assertSame(source, event.getSource());
        Assertions.assertSame(properties, event.getDtpProperties());
    }

    @Test
    void testRefreshEventKeepsSourceAndProperties() {
        Object source = new Object();
        DtpProperties properties = DtpProperties.getInstance();

        RefreshEvent event = new RefreshEvent(source, properties);

        Assertions.assertSame(source, event.getSource());
        Assertions.assertSame(properties, event.getDtpProperties());
    }

    @Test
    void testCustomContextRefreshedEventKeepsSource() {
        Object source = new Object();

        CustomContextRefreshedEvent event = new CustomContextRefreshedEvent(source);

        Assertions.assertSame(source, event.getSource());
    }
}
