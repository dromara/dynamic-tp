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

import java.util.Map;

/**
 * Interface for managing context in the application.
 * Provides methods to access beans, set context, handle events,
 * and retrieve environment properties.
 *
 * @author vzer200
 * @since 1.2.0
 */
public interface ContextManager {

    /**
     * Retrieves a bean by its class type.
     *
     * @param <T> the type of the bean
     * @param clazz the class of the bean
     * @return an instance of the bean, or null if not found
     */
    <T> T getBean(Class<T> clazz);

    /**
     * Retrieves a bean by its name and class type.
     *
     * @param <T> the type of the bean
     * @param name the name of the bean
     * @param clazz the class of the bean
     * @return an instance of the bean, or null if not found
     */
    <T> T getBean(String name, Class<T> clazz);

    /**
     * Retrieves all beans of the specified type.
     *
     * @param <T> the type of the beans
     * @param clazz the class of the beans
     * @return a map of bean names to bean instances
     */
    <T> Map<String, T> getBeansOfType(Class<T> clazz);

    /**
     * Retrieves the environment.
     *
     * @return the environment object
     */
    Object getEnvironment();

    /**
     * Retrieves an environment property by its key.
     *
     * @param key the key of the property
     * @return the value of the property, or null if not found
     */
    String getEnvironmentProperty(String key);

    /**
     * Retrieves an environment property by its key in the specified environment.
     *
     * @param key the key of the property
     * @param environment the specified environment object
     * @return the value of the property, or null if not found
     */
    String getEnvironmentProperty(String key, Object environment);

    /**
     * Retrieves an environment property by its key, with a default value.
     *
     * @param key the key of the property
     * @param defaultValue the default value to return if the property is not found
     * @return the value of the property, or the default value if not found
     */
    String getEnvironmentProperty(String key, String defaultValue);
}
