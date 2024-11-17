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

package org.dromara.dynamictp.common.manager;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.util.ExtensionServiceLoader;

import java.util.Map;

/**
 * Helper class for accessing ContextManager.
 *
 * @author vzer200
 * @since 1.2.0
 */
@Slf4j
public class ContextManagerHelper {

    private static ContextManager contextManager;

    static {
        contextManager = ExtensionServiceLoader.getFirst(ContextManager.class);
        if (contextManager == null) {
            contextManager = new NullContextManager();
            throw new IllegalStateException("No ContextManager implementation found");
        }
    }

    public static <T> T getBean(Class<T> clazz) {
        return contextManager.getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return contextManager.getBean(name, clazz);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return contextManager.getBeansOfType(clazz);
    }

    public static Object getEnvironment() {
        return contextManager.getEnvironment();
    }

    public static String getEnvironmentProperty(String key) {
        return contextManager.getEnvironmentProperty(key);
    }

    public static String getEnvironmentProperty(String key, Object environment) {
        return contextManager.getEnvironmentProperty(key, environment);
    }

    public static String getEnvironmentProperty(String key, String defaultValue) {
        return contextManager.getEnvironmentProperty(key, defaultValue);
    }
}

