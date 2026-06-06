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

import org.dromara.dynamictp.common.plugin.DtpIntercepts;
import org.dromara.dynamictp.common.plugin.DtpInterceptor;
import org.dromara.dynamictp.common.plugin.DtpInterceptorProxyFactory;
import org.dromara.dynamictp.common.plugin.DtpInvocation;
import org.dromara.dynamictp.common.plugin.DtpSignature;
import org.dromara.dynamictp.common.plugin.PluginException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * DtpInterceptorProxyFactoryTest related.
 */
class DtpInterceptorProxyFactoryTest {

    @Test
    void testEnhanceReturnsOriginalTargetWhenNoMatchingSignature() {
        TargetService target = new TargetService();

        Object enhanced = DtpInterceptorProxyFactory.enhance(target, new NotMatchedInterceptor());

        Assertions.assertSame(target, enhanced);
    }

    @Test
    void testEnhanceThrowsWhenInterceptorWithoutAnnotation() {
        TargetService target = new TargetService();

        PluginException ex = Assertions.assertThrows(PluginException.class,
                () -> DtpInterceptorProxyFactory.enhance(target, new MissingAnnotationInterceptor()));

        Assertions.assertTrue(ex.getMessage().contains("No @DtpIntercepts annotation was found"));
    }

    @Test
    void testEnhanceThrowsWhenSignatureMethodNotFound() {
        TargetService target = new TargetService();

        PluginException ex = Assertions.assertThrows(PluginException.class,
                () -> DtpInterceptorProxyFactory.enhance(target, new WrongMethodInterceptor()));

        Assertions.assertTrue(ex.getMessage().contains("Could not find method"));
    }

    @Test
    void testEnhanceUsesInterceptorWhenSignatureMatches() {
        TargetService target = new TargetService("ok-");
        DtpInterceptor interceptor = new MatchedInterceptor();
        TargetService enhanced = (TargetService) DtpInterceptorProxyFactory.enhance(target,
                new Class<?>[] {String.class}, new Object[] {"init-"}, interceptor);

        String result = enhanced.join("a", "b");

        Assertions.assertEquals("intercepted:ok-a-b", result);
    }

    public static class TargetService {

        private final String prefix;

        public TargetService() {
            this("default-");
        }

        public TargetService(String prefix) {
            this.prefix = prefix;
        }

        public String join(String left, String right) {
            return prefix + left + "-" + right;
        }
    }

    @DtpIntercepts(name = "notMatched", signatures = {
            @DtpSignature(clazz = String.class, method = "trim", args = {})
    })
    private static class NotMatchedInterceptor implements DtpInterceptor {

        @Override
        public Object intercept(DtpInvocation invocation) {
            return "intercepted";
        }
    }

    private static class MissingAnnotationInterceptor implements DtpInterceptor {

        @Override
        public Object intercept(DtpInvocation invocation) {
            return "intercepted";
        }
    }

    @DtpIntercepts(name = "wrongMethod", signatures = {
            @DtpSignature(clazz = TargetService.class, method = "notExists", args = {})
    })
    private static class WrongMethodInterceptor implements DtpInterceptor {

        @Override
        public Object intercept(DtpInvocation invocation) {
            return "intercepted";
        }
    }

    @DtpIntercepts(name = "matched", signatures = {
            @DtpSignature(clazz = TargetService.class, method = "join", args = {String.class, String.class})
    })
    private static class MatchedInterceptor implements DtpInterceptor {

        @Override
        public Object intercept(DtpInvocation invocation) throws Throwable {
            return "intercepted:" + invocation.proceed();
        }
    }
}
