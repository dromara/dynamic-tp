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

package org.dromara.dynamictp.test.core.metric;

import com.codahale.metrics.Snapshot;
import org.dromara.dynamictp.core.metric.MMAPCounter;
import org.dromara.dynamictp.core.metric.MMACounter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * MMAPCounterTest related.
 */
class MMAPCounterTest {

    @Test
    void testAddUpdatesMmaCounterAndSnapshot() {
        MMAPCounter counter = new MMAPCounter();

        counter.add(10);
        counter.add(20);
        counter.add(30);

        MMACounter mmaCounter = counter.getMmaCounter();
        Snapshot snapshot = counter.getSnapshot();
        Assertions.assertEquals(3, mmaCounter.getCount());
        Assertions.assertEquals(60, mmaCounter.getTotal());
        Assertions.assertEquals(10, mmaCounter.getMin());
        Assertions.assertEquals(30, mmaCounter.getMax());
        Assertions.assertEquals(20.0, mmaCounter.getAvg(), 0.001);
        Assertions.assertEquals(3, snapshot.size());
        Assertions.assertEquals(10, snapshot.getMin());
        Assertions.assertEquals(30, snapshot.getMax());
    }

    @Test
    void testResetClearsCounterAndSnapshot() {
        MMAPCounter counter = new MMAPCounter();
        counter.add(10);
        counter.add(20);

        counter.reset();

        MMACounter mmaCounter = counter.getMmaCounter();
        Snapshot snapshot = counter.getSnapshot();
        Assertions.assertEquals(0, mmaCounter.getCount());
        Assertions.assertEquals(0, mmaCounter.getTotal());
        Assertions.assertEquals(0, mmaCounter.getMin());
        Assertions.assertEquals(0, mmaCounter.getMax());
        Assertions.assertEquals(0.0, mmaCounter.getAvg(), 0.001);
        Assertions.assertEquals(0, snapshot.size());
    }
}
