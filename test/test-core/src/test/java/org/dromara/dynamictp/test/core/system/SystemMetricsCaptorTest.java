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

package org.dromara.dynamictp.test.core.system;

import org.dromara.dynamictp.core.system.CpuMetricsCaptor;
import org.dromara.dynamictp.core.system.MemoryMetricsCaptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * System metrics captor test.
 *
 * @author codex
 */
class SystemMetricsCaptorTest {

    @Test
    void testCpuMetricsCaptorInitialValue() {
        CpuMetricsCaptor captor = new CpuMetricsCaptor();

        assertEquals(-1.0, captor.getProcessCpuUsage(), 0.001);
    }

    @Test
    void testCpuMetricsCaptorRunDoesNotThrow() {
        CpuMetricsCaptor captor = new CpuMetricsCaptor();

        assertDoesNotThrow(captor::run);
        assertFalse(Double.isNaN(captor.getProcessCpuUsage()));
    }

    @Test
    void testMemoryMetricsCaptorInitialValue() {
        MemoryMetricsCaptor captor = new MemoryMetricsCaptor();

        assertEquals(-1.0, captor.getLongLivedMemoryUsage(), 0.001);
    }

    @Test
    void testMemoryMetricsCaptorRunDoesNotThrow() {
        MemoryMetricsCaptor captor = new MemoryMetricsCaptor();

        assertDoesNotThrow(captor::run);
        double usage = captor.getLongLivedMemoryUsage();
        assertTrue(usage == -1.0 || usage >= 0.0);
    }
}
