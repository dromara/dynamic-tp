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

package org.dromara.dynamictp.test.core.executor;

import org.dromara.dynamictp.core.executor.NamedThreadFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * NamedThreadFactoryTest related
 *
 * @author Copilot
 */
public class NamedThreadFactoryTest {

    @Test
    public void testNewThreadUsesPrefixDaemonAndPriority() {
        NamedThreadFactory factory = new NamedThreadFactory("worker", true, Thread.MAX_PRIORITY);

        Thread first = factory.newThread(() -> { });
        Thread second = factory.newThread(() -> { });

        Assert.assertEquals("worker-1", first.getName());
        Assert.assertEquals("worker-2", second.getName());
        Assert.assertTrue(first.isDaemon());
        Assert.assertEquals(Thread.MAX_PRIORITY, first.getPriority());
        Assert.assertSame(Thread.currentThread().getThreadGroup(), first.getThreadGroup());
    }

    @Test
    public void testSetNamePrefixAffectsSubsequentThreads() {
        NamedThreadFactory factory = new NamedThreadFactory("worker");

        factory.setNamePrefix("biz");
        Thread thread = factory.newThread(() -> { });

        Assert.assertEquals("biz", factory.getNamePrefix());
        Assert.assertEquals("biz-1", thread.getName());
        Assert.assertFalse(thread.isDaemon());
        Assert.assertEquals(Thread.NORM_PRIORITY, thread.getPriority());
    }
}
