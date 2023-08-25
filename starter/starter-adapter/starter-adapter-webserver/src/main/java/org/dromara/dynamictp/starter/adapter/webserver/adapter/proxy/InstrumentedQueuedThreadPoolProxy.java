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

package org.dromara.dynamictp.starter.adapter.webserver.adapter.proxy;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.jetty.InstrumentedQueuedThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.aware.AwareManager;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;

/**
 * @author hanli
 * @date 2023年08月23日 09:49
 */
@Slf4j
public class InstrumentedQueuedThreadPoolProxy extends InstrumentedQueuedThreadPool {

    private final InstrumentedQueuedThreadPool original;

    public InstrumentedQueuedThreadPoolProxy(InstrumentedQueuedThreadPool original, MeterRegistry registry, Iterable<Tag> tags, int maxThreads, int minThreads, int idleTimeout, BlockingQueue<Runnable> queue) {
        super(registry, tags, maxThreads, minThreads, idleTimeout, queue);
        this.original = original;
    }

    @Override
    public void execute(Runnable runnable) {
        AwareManager.executeEnhance(original, runnable);
        try {
            super.execute(runnable);
        } catch (RejectedExecutionException e) {
            AwareManager.beforeReject(runnable, original, log);
            throw e;
        }
    }
}
