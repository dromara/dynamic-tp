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

import org.dromara.dynamictp.core.support.task.runnable.EnhancedRunnable;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * EnhancedRunnable test.
 *
 * @author codex
 */
class EnhancedRunnableTest {

    @Test
    void testNullRunnableReturnsQuietly() {
        EnhancedRunnable runnable = EnhancedRunnable.of(null, mock(Executor.class));

        assertDoesNotThrow(runnable::run);
    }

    @Test
    void testRunDelegatesToOriginRunnable() {
        AtomicBoolean executed = new AtomicBoolean(false);
        EnhancedRunnable runnable = EnhancedRunnable.of(() -> executed.set(true), mock(Executor.class));

        runnable.run();

        assertTrue(executed.get());
    }

    @Test
    void testRunRethrowsOriginException() {
        RuntimeException expected = new RuntimeException("boom");
        EnhancedRunnable runnable = EnhancedRunnable.of(() -> {
            throw expected;
        }, mock(Executor.class));

        RuntimeException actual = assertThrows(RuntimeException.class, runnable::run);
        assertSame(expected, actual);
    }
}
