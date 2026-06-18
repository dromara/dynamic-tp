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

package org.dromara.dynamictp.core.executor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Factory that creates a virtual-thread-per-task {@link ExecutorService} via reflection,
 * so the source level can stay on Java 17 while running on Java 21+.
 *
 * <p>It is the only entry point that touches {@code Thread.ofVirtual()}, keeping every
 * other module free of Java 21 API references.</p>
 *
 * @author yanhom
 * @since 1.x.x
 */
@Slf4j
public final class VirtualThreadExecutorFactory {

    private VirtualThreadExecutorFactory() {
    }

    /**
     * Create a thread-per-task executor backed by virtual threads.
     *
     * <p>Both the virtual-thread {@code ThreadFactory} (via {@code Thread.ofVirtual()})
     * and {@code Executors.newThreadPerTaskExecutor} are JDK 21 API, so the whole chain
     * is built reflectively to keep this module compilable on Java 17 source level.</p>
     *
     * @param namePrefix the thread name prefix, fallback to a default when blank
     * @return an {@link ExecutorService} that spawns one virtual thread per task
     * @throws IllegalStateException if the runtime is older than Java 21
     */
    public static ExecutorService newThreadPerTaskExecutor(String namePrefix) {
        ThreadFactory factory = newVirtualThreadFactory(namePrefix);
        return newThreadPerTaskExecutor(factory);
    }

    /**
     * Reflectively call {@code Executors.newThreadPerTaskExecutor(ThreadFactory)},
     * which only exists on JDK 21+.
     */
    private static ExecutorService newThreadPerTaskExecutor(ThreadFactory factory) {
        try {
            return (ExecutorService) MethodUtils.invokeStaticMethod(
                    Executors.class, "newThreadPerTaskExecutor",
                    new Object[]{factory}, new Class[]{ThreadFactory.class});
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to create thread-per-task executor via reflection", e);
        }
    }

    /**
     * Reflectively build a virtual-thread {@link ThreadFactory} with the given name prefix.
     * Equivalent to {@code Thread.ofVirtual().name(prefix, 0).factory()}.
     */
    private static ThreadFactory newVirtualThreadFactory(String namePrefix) {
        String prefix = StringUtils.isNotBlank(namePrefix) ? namePrefix : "dynamic-tp-virtual";
        try {
            // Thread.Builder builder = Thread.ofVirtual();
            Class<?> threadClass = Class.forName("java.lang.Thread");
            Object builder = MethodUtils.invokeStaticMethod(threadClass, "ofVirtual");
            // builder = builder.name(prefix, 0);
            builder = MethodUtils.invokeMethod(builder, "name", prefix, 0);
            // return builder.factory();
            Object factory = MethodUtils.invokeMethod(builder, "factory");
            return (ThreadFactory) factory;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Virtual thread executor requires JDK 21+, current JRE is "
                            + System.getProperty("java.version") + ", prefix: " + prefix, e);
        }
    }
}
