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

import org.dromara.dynamictp.common.util.ConstructorUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ConstructorUtilTest related
 *
 * @author codex
 */
class ConstructorUtilTest {

    @Test
    void testBuildTpExecutorConstructorArgs() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,
                2,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                Thread::new,
                new ThreadPoolExecutor.CallerRunsPolicy());

        Object[] args = ConstructorUtil.buildTpExecutorConstructorArgs(executor);

        Assertions.assertEquals(1, args[0]);
        Assertions.assertEquals(2, args[1]);
        Assertions.assertEquals(30000L, args[2]);
        Assertions.assertEquals(TimeUnit.MILLISECONDS, args[3]);
        Assertions.assertSame(executor.getQueue(), args[4]);
        Assertions.assertSame(executor.getThreadFactory(), args[5]);
        Assertions.assertSame(executor.getRejectedExecutionHandler(), args[6]);
        executor.shutdownNow();
    }

    @Test
    void testBuildTpExecutorConstructorArgTypes() {
        Class<?>[] argTypes = ConstructorUtil.buildTpExecutorConstructorArgTypes();

        Assertions.assertArrayEquals(new Class[] {
                int.class,
                int.class,
                long.class,
                TimeUnit.class,
                BlockingQueue.class,
                ThreadFactory.class,
                RejectedExecutionHandler.class
        }, argTypes);
    }

    @Test
    void testBuildConstructorArgsReturnsEmptyTypes() {
        Assertions.assertArrayEquals(new Class[0], ConstructorUtil.buildConstructorArgs());
    }

    @Test
    void testBuildConstructorArgTypesUsesPublicConstructor() {
        Class<?>[] argTypes = ConstructorUtil.buildConstructorArgTypes(new ArrayBlockingQueue<>(1));

        Assertions.assertTrue(argTypes.length > 0);
    }
}
