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

import org.dromara.dynamictp.core.support.task.runnable.NamedRunnable;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * NamedRunnableTest related
 *
 * @author Copilot
 */
public class NamedRunnableTest {

    @Test
    public void testOfUsesExplicitNameAndDelegatesRun() {
        AtomicBoolean executed = new AtomicBoolean(false);

        NamedRunnable namedRunnable = NamedRunnable.of(() -> executed.set(true), "explicitName");
        namedRunnable.run();

        Assert.assertEquals("explicitName", namedRunnable.getName());
        Assert.assertTrue(executed.get());
    }

    @Test
    public void testOfGeneratesNameWhenBlank() {
        NamedRunnable namedRunnable = NamedRunnable.of(new TestRunnable(), " ");

        Assert.assertTrue(namedRunnable.getName().startsWith("TestRunnable-"));
    }

    private static class TestRunnable implements Runnable {

        @Override
        public void run() {
        }
    }
}
