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

package org.dromara.dynamictp.common.pattern.filter;

/**
 * InvokerChainFactory related
 *
 * @author yanhom
 * @since 1.0.8
 **/
public final class InvokerChainFactory {

    private InvokerChainFactory() { }

    @SafeVarargs
    public static<T> InvokerChain<T> buildInvokerChain(Invoker<T> target, Filter<T>... filters) {

        InvokerChain<T> invokerChain = new InvokerChain<>();
        Invoker<T> last = target;
        for (int i = filters.length - 1; i >= 0; i--) {
            Invoker<T> next = last;
            Filter<T> filter = filters[i];
            last = context -> filter.doFilter(context, next);
        }
        invokerChain.setHead(last);
        return invokerChain;
    }
}


