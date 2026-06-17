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

package org.dromara.dynamictp.test.common.pattern.filter;

import org.dromara.dynamictp.common.pattern.filter.Filter;
import org.dromara.dynamictp.common.pattern.filter.Invoker;
import org.dromara.dynamictp.common.pattern.filter.InvokerChain;
import org.dromara.dynamictp.common.pattern.filter.InvokerChainFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * InvokerChainFactoryTest related
 *
 * @author codex
 */
class InvokerChainFactoryTest {

    @Test
    void testBuildInvokerChainRunsFiltersInDeclaredOrderBeforeTarget() {
        List<String> calls = new ArrayList<>();
        Invoker<String> target = context -> calls.add("target:" + context);
        Filter<String> first = new Filter<String>() {
            @Override
            public int getOrder() {
                return 1;
            }

            @Override
            public void doFilter(String context, Invoker<String> nextInvoker) {
                calls.add("first:before");
                nextInvoker.invoke(context);
                calls.add("first:after");
            }
        };
        Filter<String> second = new Filter<String>() {
            @Override
            public int getOrder() {
                return 2;
            }

            @Override
            public void doFilter(String context, Invoker<String> nextInvoker) {
                calls.add("second:before");
                nextInvoker.invoke(context);
                calls.add("second:after");
            }
        };

        InvokerChain<String> chain = InvokerChainFactory.buildInvokerChain(target, first, second);
        chain.proceed("ctx");

        Assertions.assertEquals(Arrays.asList(
                "first:before",
                "second:before",
                "target:ctx",
                "second:after",
                "first:after"), calls);
    }

    @Test
    void testBuildInvokerChainWithoutFiltersInvokesTargetDirectly() {
        List<String> calls = new ArrayList<>();
        InvokerChain<String> chain = InvokerChainFactory.buildInvokerChain(calls::add);

        chain.proceed("target");

        Assertions.assertEquals(Arrays.asList("target"), calls);
    }
}
