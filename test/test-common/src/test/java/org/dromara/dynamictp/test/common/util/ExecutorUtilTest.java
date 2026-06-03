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

package org.dromara.dynamictp.test.common.util;

import org.dromara.dynamictp.common.util.ExecutorUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.FutureTask;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.TRACE_ID;

/**
 * ExecutorUtilTest related.
 */
class ExecutorUtilTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void testTryClearContextRemovesTraceId() {
        MDC.put(TRACE_ID, "trace-1");

        ExecutorUtil.tryClearContext();

        Assertions.assertNull(MDC.get(TRACE_ID));
    }

    @Test
    void testTryExecAfterExecuteClearsContextForPlainRunnable() {
        MDC.put(TRACE_ID, "trace-2");

        ExecutorUtil.tryExecAfterExecute(() -> { }, null);

        Assertions.assertNull(MDC.get(TRACE_ID));
    }

    @Test
    void testTryExecAfterExecuteClearsContextWhenThrowableExists() {
        MDC.put(TRACE_ID, "trace-3");

        ExecutorUtil.tryExecAfterExecute(() -> { }, new IllegalStateException("boom"));

        Assertions.assertNull(MDC.get(TRACE_ID));
    }

    @Test
    void testTryExecAfterExecuteReadsCompletedFutureTask() {
        MDC.put(TRACE_ID, "trace-4");
        FutureTask<String> futureTask = new FutureTask<>(() -> "done");
        futureTask.run();

        ExecutorUtil.tryExecAfterExecute(futureTask, null);

        Assertions.assertNull(MDC.get(TRACE_ID));
        Assertions.assertTrue(futureTask.isDone());
    }

    @Test
    void testTryExecAfterExecuteHandlesFutureTaskException() {
        MDC.put(TRACE_ID, "trace-5");
        FutureTask<String> futureTask = new FutureTask<>(() -> {
            throw new IllegalArgumentException("failed");
        });
        futureTask.run();

        ExecutorUtil.tryExecAfterExecute(futureTask, null);

        Assertions.assertNull(MDC.get(TRACE_ID));
    }
}
