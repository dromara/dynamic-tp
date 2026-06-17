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

package org.dromara.dynamictp.test.common.plugin;

import org.dromara.dynamictp.common.plugin.DtpInvocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * DtpInvocationTest related.
 */
public class DtpInvocationTest {

    @Test
    void testGettersReturnConstructorArguments() throws Exception {
        TargetService target = new TargetService();
        Method method = TargetService.class.getDeclaredMethod("join", String.class, String.class);
        Object[] args = {"a", "b"};

        DtpInvocation invocation = new DtpInvocation(target, method, args);

        Assertions.assertSame(target, invocation.getTarget());
        Assertions.assertSame(method, invocation.getMethod());
        Assertions.assertSame(args, invocation.getArgs());
    }

    @Test
    void testProceedInvokesTargetMethod() throws Exception {
        TargetService target = new TargetService();
        Method method = TargetService.class.getDeclaredMethod("join", String.class, String.class);
        DtpInvocation invocation = new DtpInvocation(target, method, new Object[] {"a", "b"});

        Object result = invocation.proceed();

        Assertions.assertEquals("a-b", result);
    }

    @Test
    void testProceedPropagatesInvocationTargetException() throws Exception {
        TargetService target = new TargetService();
        Method method = TargetService.class.getDeclaredMethod("fail");
        DtpInvocation invocation = new DtpInvocation(target, method, new Object[0]);

        InvocationTargetException exception = Assertions.assertThrows(InvocationTargetException.class, invocation::proceed);

        Assertions.assertInstanceOf(IllegalStateException.class, exception.getCause());
    }

    public static class TargetService {

        public String join(String left, String right) {
            return left + "-" + right;
        }

        public void fail() {
            throw new IllegalStateException("failed");
        }
    }
}
