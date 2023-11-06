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

package org.dromara.dynamictp.common.util;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unified ServiceLoader Helper
 *
 * @author xs.Tao
 * @since 1.1.4
 */
public class ExtensionServiceLoader {

    private static final Map<Class<?>, List<?>> EXTENSION_MAP = new ConcurrentHashMap<>();

    private ExtensionServiceLoader() { }

    /**
     * load service
     * @param clazz SPI interface
     * @return services
     * @param <T> interface class
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> get(Class<T> clazz) {
        List<T> services = (List<T>) EXTENSION_MAP.get(clazz);
        if (CollectionUtils.isEmpty(services)) {
            services = load(clazz);
            if (CollectionUtils.isNotEmpty(services)) {
                EXTENSION_MAP.put(clazz, services);
            }
        }
        return services;
    }

    /**
     * load the first service
     * @param clazz SPI interface
     * @return service
     * @param <T> interface class
     */
    public static <T> T getFirst(Class<T> clazz) {
        List<T> services = get(clazz);
        return CollectionUtils.isEmpty(services) ? null : services.get(0);
    }

    private static <T> List<T> load(Class<T> clazz) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
        List<T> services = new ArrayList<>();
        for (T service : serviceLoader) {
            services.add(service);
        }
        return services;
    }
}
