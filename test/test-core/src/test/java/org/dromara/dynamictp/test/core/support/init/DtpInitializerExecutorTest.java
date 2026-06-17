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

package org.dromara.dynamictp.test.core.support.init;

import org.dromara.dynamictp.common.util.ExtensionServiceLoader;
import org.dromara.dynamictp.core.support.init.DtpInitializer;
import org.dromara.dynamictp.core.support.init.DtpInitializerExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DtpInitializerExecutor test.
 *
 * @author codex
 */
@Execution(ExecutionMode.SAME_THREAD)
class DtpInitializerExecutorTest {

    private Object originalInitializers;

    @BeforeEach
    void setUp() throws Exception {
        resetInitialized();
        originalInitializers = extensionMap().get(DtpInitializer.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        resetInitialized();
        if (originalInitializers == null) {
            extensionMap().remove(DtpInitializer.class);
        } else {
            extensionMap().put(DtpInitializer.class, originalInitializers);
        }
    }

    @Test
    void testInitRunsInitializersInOrderOnlyOnce() throws Exception {
        List<String> calls = new ArrayList<>();
        DtpInitializer later = initializer("later", 20, calls);
        DtpInitializer earlier = initializer("earlier", 10, calls);
        extensionMap().put(DtpInitializer.class, new ArrayList<>(Arrays.asList(later, earlier)));

        DtpInitializerExecutor.init("arg");
        DtpInitializerExecutor.init("ignored");

        assertEquals(Arrays.asList("earlier:arg", "later:arg"), calls);
    }

    private DtpInitializer initializer(String name, int order, List<String> calls) {
        return new DtpInitializer() {
            @Override
            public int getOrder() {
                return order;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public void init(Object... args) {
                calls.add(name + ":" + args[0]);
            }
        };
    }

    private void resetInitialized() throws Exception {
        Field field = DtpInitializerExecutor.class.getDeclaredField("INITIALIZED");
        field.setAccessible(true);
        ((AtomicBoolean) field.get(null)).set(false);
    }

    @SuppressWarnings("unchecked")
    private Map<Class<?>, Object> extensionMap() throws Exception {
        Field field = ExtensionServiceLoader.class.getDeclaredField("EXTENSION_MAP");
        field.setAccessible(true);
        return (Map<Class<?>, Object>) field.get(null);
    }
}
