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

import java.lang.reflect.Method;
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
 * <p><b>Reflection notes</b> (same approach as Tomcat's {@code Jre21Compat}):
 * {@code Thread.ofVirtual()} returns {@code java.lang.ThreadBuilders$VirtualThreadBuilder},
 * a non-public class in the {@code java.lang} package. Resolving methods on that runtime
 * class forces {@code setAccessible(true)}, which fails with
 * {@link java.lang.reflect.InaccessibleObjectException} unless the application is started
 * with {@code --add-opens java.base/java.lang=ALL-UNNAMED}. Methods are therefore resolved
 * from the public, exported interface {@code java.lang.Thread$Builder}, whose access check
 * passes without opening the module.</p>
 *
 * @author yanhom
 * @since 1.3.0
 */
@Slf4j
public final class VirtualThreadExecutorFactory {

    private static final String DEFAULT_NAME_PREFIX = "dynamic-tp-virtual";

    /**
     * {@code Thread.ofVirtual()}, JDK 21+.
     */
    private static final Method OF_VIRTUAL_METHOD;

    /**
     * {@code Thread.Builder#name(String, long)}, resolved on the public interface.
     */
    private static final Method NAME_METHOD;

    /**
     * {@code Thread.Builder#factory()}, resolved on the public interface.
     */
    private static final Method FACTORY_METHOD;

    /**
     * {@code Executors.newThreadPerTaskExecutor(ThreadFactory)}, JDK 21+.
     */
    private static final Method THREAD_PER_TASK_EXECUTOR_METHOD;

    private static final boolean SUPPORTED;

    static {
        Class<?> sequencedCollectionClass = null;
        Method ofVirtual = null;
        Method name = null;
        Method factory = null;
        Method threadPerTaskExecutor = null;
        try {
            // Virtual threads existed as a preview feature before JDK 21, so test for
            // another class that was added in JDK 21 to detect the runtime accurately.
            sequencedCollectionClass = Class.forName("java.util.SequencedCollection");
            Class<?> builderClass = Class.forName("java.lang.Thread$Builder");
            ofVirtual = Thread.class.getMethod("ofVirtual");
            name = builderClass.getMethod("name", String.class, long.class);
            factory = builderClass.getMethod("factory");
            threadPerTaskExecutor = Executors.class.getMethod("newThreadPerTaskExecutor", ThreadFactory.class);
        } catch (ClassNotFoundException e) {
            // must be pre JDK 21
            log.debug("DynamicTp virtual thread executor is not supported, current JRE is {}",
                    System.getProperty("java.version"));
        } catch (ReflectiveOperationException e) {
            // should never happen
            log.error("DynamicTp virtual thread executor init failed unexpectedly.", e);
        }
        SUPPORTED = sequencedCollectionClass != null && ofVirtual != null;
        OF_VIRTUAL_METHOD = ofVirtual;
        NAME_METHOD = name;
        FACTORY_METHOD = factory;
        THREAD_PER_TASK_EXECUTOR_METHOD = threadPerTaskExecutor;
    }

    private VirtualThreadExecutorFactory() {
    }

    /**
     * Whether the current runtime supports virtual threads, i.e. JDK 21+.
     *
     * @return true if virtual threads are available
     */
    public static boolean isSupported() {
        return SUPPORTED;
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
        String prefix = StringUtils.isNotBlank(namePrefix) ? namePrefix : DEFAULT_NAME_PREFIX;
        if (!SUPPORTED) {
            throw new IllegalStateException("Virtual thread executor requires JDK 21+, current JRE is "
                    + System.getProperty("java.version") + ", prefix: " + prefix);
        }
        try {
            // Thread.Builder.OfVirtual builder = Thread.ofVirtual().name(prefix, 0);
            Object builder = OF_VIRTUAL_METHOD.invoke(null);
            builder = NAME_METHOD.invoke(builder, prefix, 0L);
            // ThreadFactory factory = builder.factory();
            ThreadFactory factory = (ThreadFactory) FACTORY_METHOD.invoke(builder);
            // return Executors.newThreadPerTaskExecutor(factory);
            return (ExecutorService) THREAD_PER_TASK_EXECUTOR_METHOD.invoke(null, factory);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to create virtual thread per task executor, prefix: " + prefix, e);
        }
    }
}
