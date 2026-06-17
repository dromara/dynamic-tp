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

package org.dromara.dynamictp.test.core.runnable;

import org.dromara.dynamictp.core.support.task.runnable.DtpRunnable;
import org.dromara.dynamictp.core.support.task.runnable.NamedRunnable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DtpRunnable and NamedRunnable test
 *
 * @author yanhom
 * @since 1.2.2
 */
class RunnableTest {

    @Test
    void testDtpRunnableRun() {
        boolean[] executed = {false};
        DtpRunnable dtpRunnable = new DtpRunnable(() -> { }, () -> executed[0] = true, "testTask");
        dtpRunnable.run();
        assertTrue(executed[0]);
    }

    @Test
    void testDtpRunnableProperties() {
        DtpRunnable dtpRunnable = new DtpRunnable(() -> { }, () -> { }, "myTask");
        assertEquals("myTask", dtpRunnable.getTaskName());
        assertNotNull(dtpRunnable.getOriginRunnable());
        assertNotNull(dtpRunnable.getRunnable());
    }

    @Test
    void testNamedRunnableWithValidName() {
        boolean[] executed = {false};
        NamedRunnable namedRunnable = NamedRunnable.of(() -> executed[0] = true, "customName");
        namedRunnable.run();
        assertTrue(executed[0]);
        assertEquals("customName", namedRunnable.getName());
    }

    @Test
    void testNamedRunnableWithBlankNameGeneratesUuid() {
        NamedRunnable namedRunnable = NamedRunnable.of(() -> { }, "");
        String name = namedRunnable.getName();
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test
    void testNamedRunnableWithNullNameGeneratesUuid() {
        NamedRunnable namedRunnable = NamedRunnable.of(() -> { }, null);
        String name = namedRunnable.getName();
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }
}
