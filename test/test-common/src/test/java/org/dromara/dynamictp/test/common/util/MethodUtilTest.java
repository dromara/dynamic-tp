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

import org.dromara.dynamictp.common.util.MethodUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

/**
 * MethodUtilTest related.
 */
public class MethodUtilTest {

    @Test
    void testInvokeAndReturnDoubleReturnsMethodValue() throws Exception {
        Method method = NumberSource.class.getDeclaredMethod("doubleValue");

        double result = MethodUtil.invokeAndReturnDouble(method, new NumberSource());

        Assertions.assertEquals(1.5D, result);
    }

    @Test
    void testInvokeAndReturnLongReturnsMethodValue() throws Exception {
        Method method = NumberSource.class.getDeclaredMethod("longValue");

        long result = MethodUtil.invokeAndReturnLong(method, new NumberSource());

        Assertions.assertEquals(2L, result);
    }

    @Test
    void testInvokeAndReturnIntReturnsMethodValue() throws Exception {
        Method method = NumberSource.class.getDeclaredMethod("intValue");

        int result = MethodUtil.invokeAndReturnInt(method, new NumberSource());

        Assertions.assertEquals(3, result);
    }

    @Test
    void testInvokeAndReturnNumberReturnsNegativeOneWhenMethodIsNull() {
        NumberSource source = new NumberSource();

        Assertions.assertEquals(-1D, MethodUtil.invokeAndReturnDouble(null, source));
        Assertions.assertEquals(-1L, MethodUtil.invokeAndReturnLong(null, source));
        Assertions.assertEquals(-1, MethodUtil.invokeAndReturnInt(null, source));
    }

    @Test
    void testInvokeAndReturnNumberReturnsNegativeOneWhenInvocationFails() throws Exception {
        Method method = NumberSource.class.getDeclaredMethod("fail");

        Assertions.assertEquals(-1D, MethodUtil.invokeAndReturnDouble(method, new NumberSource()));
        Assertions.assertEquals(-1L, MethodUtil.invokeAndReturnLong(method, new NumberSource()));
        Assertions.assertEquals(-1, MethodUtil.invokeAndReturnInt(method, new NumberSource()));
    }

    public static class NumberSource {

        public double doubleValue() {
            return 1.5D;
        }

        public long longValue() {
            return 2L;
        }

        public int intValue() {
            return 3;
        }

        public String fail() {
            return "failed";
        }
    }
}
