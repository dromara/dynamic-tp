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

package org.dromara.dynamictp.common.plugin;

import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * DtpInvocationHandler related
 *
 * @author yanhom
 * @since 1.2.1
 */
public class DtpInvocationHandler implements InvocationHandler {

    private final Object target;

    private final DtpInterceptor interceptor;

    private final Map<Class<?>, Set<Method>> signatureMap;

    public DtpInvocationHandler(Object target, DtpInterceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
        this.target = target;
        this.interceptor = interceptor;
        this.signatureMap = signatureMap;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Set<Method> methods = signatureMap.get(method.getDeclaringClass());
        if (CollectionUtils.isNotEmpty(methods) && methods.contains(method)) {
            return interceptor.intercept(new DtpInvocation(target, method, args));
        }
        return method.invoke(target, args);
    }
}
