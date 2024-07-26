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
// Copyright (c) 2007-2020 VMware, Inc. or its affiliates.  All rights reserved.
//
// This software, the RabbitMQ Java client library, is triple-licensed under the
// Mozilla Public License 2.0 ("MPL"), the GNU General Public License version 2
// ("GPL") and the Apache License version 2 ("ASL"). For the MPL, please see
// LICENSE-MPL-RabbitMQ. For the GPL, please see LICENSE-GPL2.  For the ASL,
// please see LICENSE-APACHE2.
//
// This software is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
// either express or implied. See the LICENSE file for specific language governing
// rights and limitations of this software.
//
// If you have any questions regarding licensing, please contact us at
// info@rabbitmq.com.

/*
 * Modifications Copyright 2015-2020 VMware, Inc. or its affiliates. and licenced as per
 * the rest of the RabbitMQ Java client.
 */

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * https://creativecommons.org/licenses/publicdomain
 */

package org.dromara.dynamictp.common.manager;

import java.util.Map;

/**
 * Interface for managing context in the application.
 * Provides methods to access beans, set context, handle events,
 * and retrieve environment properties and profiles.
 *
 * @author vzer200
 * @since 1.0.0
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
     * Sets the context.
     *
     * @param context the context to set
     */
    void setContext(Object context);

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
     * Retrieves an environment property by its key, with a default value.
     *
     * @param key the key of the property
     * @param defaultValue the default value to return if the property is not found
     * @return the value of the property, or the default value if not found
     */
    String getEnvironmentProperty(String key, String defaultValue);

    /**
     * Retrieves the active profiles.
     *
     * @return an array of active profile names
     */
    String[] getActiveProfiles();

    /**
     * Retrieves the default profiles.
     *
     * @return an array of default profile names
     */
    String[] getDefaultProfiles();
}
