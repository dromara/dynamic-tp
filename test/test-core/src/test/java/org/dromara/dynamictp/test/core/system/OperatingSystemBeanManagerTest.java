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

import org.dromara.dynamictp.core.system.OperatingSystemBeanManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OperatingSystemBeanManager test
 *
 * @author yanhom
 * @since 1.2.2
 */
class OperatingSystemBeanManagerTest {

    @Test
    void shouldExposePlatformOperatingSystemBean() {
        assertNotNull(OperatingSystemBeanManager.getOperatingSystemBean());
    }

    @Test
    void shouldReadCpuAndMemoryMetricsWithoutInvalidNegativeValues() {
        double systemCpuUsage = OperatingSystemBeanManager.getSystemCpuUsage();
        long processCpuTime = OperatingSystemBeanManager.getProcessCpuTime();
        long totalPhysicalMemory = OperatingSystemBeanManager.getTotalPhysicalMem();
        long freePhysicalMemory = OperatingSystemBeanManager.getFreePhysicalMem();

        assertTrue(systemCpuUsage >= -1.0);
        assertTrue(processCpuTime >= 0);
        assertTrue(totalPhysicalMemory >= 0);
        assertTrue(freePhysicalMemory >= 0);
    }
}
