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

import org.dromara.dynamictp.common.plugin.DtpInterceptor;
import org.dromara.dynamictp.common.plugin.DtpInvocation;
import org.dromara.dynamictp.common.plugin.DtpInvocationHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * DtpInvocationHandlerTest related.
 */
public class DtpInvocationHandlerTest {

    @Test
    void testInvokeUsesInterceptorWhenMethodMatchesSignature() throws Throwable {
        TargetService target = new TargetService();
        Method method = TargetService.class.getMethod("join", String.class, String.class);
        RecordingInterceptor interceptor = new RecordingInterceptor();
        DtpInvocationHandler handler = new DtpInvocationHandler(target, interceptor, signatureMap(method));

        Object result = handler.invoke(target, method, new Object[] {"a", "b"});

        Assertions.assertEquals("intercepted:a-b", result);
        Assertions.assertTrue(interceptor.invoked);
    }

    @Test
    void testInvokeCallsTargetWhenMethodDoesNotMatchSignature() throws Throwable {
        TargetService target = new TargetService();
        Method method = TargetService.class.getMethod("join", String.class, String.class);
        DtpInvocationHandler handler = new DtpInvocationHandler(target, invocation -> "intercepted", Collections.emptyMap());

        Object result = handler.invoke(target, method, new Object[] {"a", "b"});

        Assertions.assertEquals("a-b", result);
    }

    private static Map<Class<?>, Set<Method>> signatureMap(Method method) {
        Set<Method> methods = new HashSet<>();
        methods.add(method);
        return Collections.singletonMap(method.getDeclaringClass(), methods);
    }

    public static class TargetService {

        public String join(String left, String right) {
            return left + "-" + right;
        }
    }

    private static class RecordingInterceptor implements DtpInterceptor {

        private boolean invoked;

        @Override
        public Object intercept(DtpInvocation invocation) throws Throwable {
            invoked = true;
            return "intercepted:" + invocation.proceed();
        }
    }
}
