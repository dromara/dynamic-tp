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

package org.dromara.dynamictp.test.core.support;

import org.dromara.dynamictp.core.support.DynamicTp;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * DynamicTp test
 *
 * @author yanhom
 * @since 1.2.2
 */
class DynamicTpTest {

    @Test
    void shouldUseEmptyNameByDefault() throws NoSuchMethodException {
        DynamicTp annotation = annotationOn("defaultExecutor");

        assertNotNull(annotation);
        assertEquals("", annotation.value());
    }

    @Test
    void shouldExposeConfiguredExecutorNameAtRuntime() throws NoSuchMethodException {
        DynamicTp annotation = annotationOn("namedExecutor");

        assertNotNull(annotation);
        assertEquals("customExecutor", annotation.value());
    }

    private DynamicTp annotationOn(String methodName) throws NoSuchMethodException {
        Method method = TestConfiguration.class.getDeclaredMethod(methodName);
        return method.getAnnotation(DynamicTp.class);
    }

    private static class TestConfiguration {

        @DynamicTp
        void defaultExecutor() {
        }

        @DynamicTp("customExecutor")
        void namedExecutor() {
        }
    }
}
