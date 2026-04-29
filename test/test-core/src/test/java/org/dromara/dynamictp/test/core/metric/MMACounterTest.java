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

import org.dromara.dynamictp.core.metric.MMACounter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MMACounter test
 *
 * @author yanhom
 * @since 1.2.2
 */
class MMACounterTest {

    @Test
    void testInitialState() {
        MMACounter counter = new MMACounter();
        assertEquals(0, counter.getCount());
        assertEquals(0, counter.getTotal());
        assertEquals(0, counter.getMin());
        assertEquals(0, counter.getMax());
        assertEquals(0.0, counter.getAvg(), 0.001);
    }

    @Test
    void testSingleAdd() {
        MMACounter counter = new MMACounter();
        counter.add(100);

        assertEquals(1, counter.getCount());
        assertEquals(100, counter.getTotal());
        assertEquals(100, counter.getMin());
        assertEquals(100, counter.getMax());
        assertEquals(100.0, counter.getAvg(), 0.001);
    }

    @Test
    void testMultipleAdds() {
        MMACounter counter = new MMACounter();
        counter.add(100);
        counter.add(200);
        counter.add(300);

        assertEquals(3, counter.getCount());
        assertEquals(600, counter.getTotal());
        assertEquals(100, counter.getMin());
        assertEquals(300, counter.getMax());
        assertEquals(200.0, counter.getAvg(), 0.001);
    }

    @Test
    void testReset() {
        MMACounter counter = new MMACounter();
        counter.add(50);
        counter.add(150);
        counter.reset();

        assertEquals(0, counter.getCount());
        assertEquals(0, counter.getTotal());
        assertEquals(0, counter.getMin());
        assertEquals(0, counter.getMax());
        assertEquals(0.0, counter.getAvg(), 0.001);
    }

    @Test
    void testResetAndReuse() {
        MMACounter counter = new MMACounter();
        counter.add(1000);
        counter.reset();
        counter.add(10);

        assertEquals(1, counter.getCount());
        assertEquals(10, counter.getTotal());
        assertEquals(10, counter.getMin());
        assertEquals(10, counter.getMax());
    }

    @Test
    void testMinUpdates() {
        MMACounter counter = new MMACounter();
        counter.add(50);
        counter.add(10);
        counter.add(30);
        counter.add(5);

        assertEquals(5, counter.getMin());
    }

    @Test
    void testMaxUpdates() {
        MMACounter counter = new MMACounter();
        counter.add(50);
        counter.add(200);
        counter.add(100);
        counter.add(500);

        assertEquals(500, counter.getMax());
    }

    @Test
    void testAvgPrecision() {
        MMACounter counter = new MMACounter();
        counter.add(1);
        counter.add(2);

        // 3/2 = 1.5, rounded to 4 decimal places
        assertEquals(1.5, counter.getAvg(), 0.0001);
    }
}
