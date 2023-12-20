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
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.util.ExtensionServiceLoader;
import org.dromara.dynamictp.common.util.StringUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * DtpInterceptorRegistry related
 *
 * @author windsearcher.lq
 * @since 1.1.4
 **/
@Slf4j
public class DtpInterceptorRegistry {

    /**
     * Maintain all automatically registered and manually registered INTERCEPTORS
     */
    private static final Map<String, DtpInterceptor> INTERCEPTORS = Maps.newConcurrentMap();

    static {
        List<DtpInterceptor> loadedInterceptors = ExtensionServiceLoader.get(DtpInterceptor.class);
        if (CollectionUtils.isNotEmpty(loadedInterceptors)) {
            loadedInterceptors.forEach(x -> {
                DtpIntercepts interceptsAnno = x.getClass().getAnnotation(DtpIntercepts.class);
                if (Objects.nonNull(interceptsAnno)) {
                    String name = StringUtils.isBlank(interceptsAnno.name()) ? x.getClass().getSimpleName() : interceptsAnno.name();
                    INTERCEPTORS.put(name, x);
                }
            });
        }
    }

    private DtpInterceptorRegistry() { }

    public static void register(String name, DtpInterceptor dtpInterceptor) {
        log.info("DynamicTp register DtpInterceptor, name: {}, interceptor: {}", name, dtpInterceptor);
        INTERCEPTORS.put(name, dtpInterceptor);
    }

    public static Map<String, DtpInterceptor> getInterceptors() {
        return Collections.unmodifiableMap(INTERCEPTORS);
    }

    public static Object pluginAll(Object target) {
        return plugin(target, INTERCEPTORS.keySet());
    }

    public static Object pluginAll(Object target, Class<?>[] argTypes, Object[] args) {
        return plugin(target, INTERCEPTORS.keySet(), argTypes, args);
    }

    public static Object plugin(Object target, Set<String> interceptors) {
        val filterInterceptors = getInterceptors(interceptors);
        for (DtpInterceptor interceptor : filterInterceptors) {
            target = interceptor.plugin(target);
        }
        return target;
    }

    public static Object plugin(Object target, Set<String> interceptors, Class<?>[] argTypes, Object[] args) {
        val filterInterceptors = getInterceptors(interceptors);
        for (DtpInterceptor interceptor : filterInterceptors) {
            target = interceptor.plugin(target, argTypes, args);
        }
        return target;
    }

    private static Collection<DtpInterceptor> getInterceptors(Set<String> interceptors) {
        if (CollectionUtils.isEmpty(interceptors)) {
            return INTERCEPTORS.values();
        }
        return INTERCEPTORS.entrySet()
                .stream()
                .filter(x -> StringUtil.containsIgnoreCase(x.getKey(), interceptors))
                .map(Map.Entry::getValue)
                .collect(toList());
    }
}
