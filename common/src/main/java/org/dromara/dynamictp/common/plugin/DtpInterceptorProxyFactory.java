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

import com.google.common.collect.Maps;
import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author windsearcher.lq
 * @since 1.1.4
 */
public class DtpInterceptorProxyFactory {

    private DtpInterceptorProxyFactory() { }

    public static Object enhance(Object target, DtpInterceptor interceptor) {
        return enhance(target, null, null, interceptor);
    }

    public static Object enhance(Object target, Class<?>[] argumentTypes, Object[] arguments, DtpInterceptor interceptor) {
        Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
        if (!signatureMap.containsKey(target.getClass())) {
            return target;
        }
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target.getClass());
        enhancer.setCallback(new DtpInterceptorProxy(target, interceptor, signatureMap));
        if (Objects.isNull(argumentTypes) || Objects.isNull(arguments)) {
            return enhancer.create();
        }
        return enhancer.create(argumentTypes, arguments);
    }

    private static Map<Class<?>, Set<Method>> getSignatureMap(DtpInterceptor interceptor) {
        DtpIntercepts interceptsAnno = interceptor.getClass().getAnnotation(DtpIntercepts.class);
        if (interceptsAnno == null) {
            throw new PluginException("No @DtpIntercepts annotation was found in interceptor " + interceptor.getClass().getName());
        }

        DtpSignature[] signatures = interceptsAnno.signatures();
        Map<Class<?>, Set<Method>> signatureMap = Maps.newHashMap();
        for (DtpSignature signature : signatures) {
            Set<Method> methods = signatureMap.computeIfAbsent(signature.clazz(), k -> new HashSet<>());
            try {
                Method method = signature.clazz().getMethod(signature.method(), signature.args());
                methods.add(method);
            } catch (NoSuchMethodException e) {
                throw new PluginException("Could not find method on " + signature.clazz() + " named " + signature.method() + ". Cause: " + e, e);
            }
        }
        return signatureMap;
    }
}
