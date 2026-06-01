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

package org.dromara.dynamictp.test.core.monitor.collector;

import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.core.monitor.collector.MicroMeterCollector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * MicroMeterCollectorTest related.
 */
class MicroMeterCollectorTest {

    @Test
    void testGaugeDoesNotThrowWhenOptionalTagValuesAreNull() {
        ThreadPoolStats stats = new ThreadPoolStats();
        stats.setPoolName("test-pool");

        MicroMeterCollector collector = new MicroMeterCollector();

        Assertions.assertDoesNotThrow(() -> collector.gauge(stats));
    }

    @Test
    void testGaugeDoesNotThrowWhenAllTagValuesAreNull() {
        ThreadPoolStats stats = new ThreadPoolStats();
        MicroMeterCollector collector = new MicroMeterCollector();

        Assertions.assertDoesNotThrow(() -> collector.gauge(stats));
    }

    @Test
    void testCollectDoesNotThrowWhenPoolNameIsNull() {
        ThreadPoolStats stats = new ThreadPoolStats();
        MicroMeterCollector collector = new MicroMeterCollector();

        Assertions.assertDoesNotThrow(() -> collector.collect(stats));
    }
}
