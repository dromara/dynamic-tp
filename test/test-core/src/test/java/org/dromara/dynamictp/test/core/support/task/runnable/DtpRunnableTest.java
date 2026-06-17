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

package org.dromara.dynamictp.test.core.support.task.runnable;

import org.dromara.dynamictp.core.support.task.runnable.DtpRunnable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.TRACE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DtpRunnableTest related.
 */
class DtpRunnableTest {

    @AfterEach
    void tearDown() {
        MDC.remove(TRACE_ID);
    }

    @Test
    void testConstructorCapturesMetadataAndTraceId() {
        Runnable origin = () -> { };
        Runnable wrapped = () -> { };
        MDC.put(TRACE_ID, "trace-1");

        DtpRunnable runnable = new DtpRunnable(origin, wrapped, "task-a");

        assertSame(origin, runnable.getOriginRunnable());
        assertSame(wrapped, runnable.getRunnable());
        assertEquals("task-a", runnable.getTaskName());
        assertEquals("trace-1", runnable.getTraceId());
    }

    @Test
    void testRunDelegatesToWrappedRunnable() {
        AtomicBoolean executed = new AtomicBoolean(false);
        DtpRunnable runnable = new DtpRunnable(() -> { }, () -> executed.set(true), "task-b");

        runnable.run();

        assertTrue(executed.get());
    }
}
