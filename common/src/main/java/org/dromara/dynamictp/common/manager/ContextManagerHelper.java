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
import org.dromara.dynamictp.common.util.ExtensionServiceLoader;

import java.util.Map;

public class ContextManagerHelper {

    private static final ContextManager CONTEXT_MANAGER = ExtensionServiceLoader.getFirst(ContextManager.class);

    static {
        if (CONTEXT_MANAGER == null) {
            throw new IllegalStateException("No ContextManager implementation found");
        }
    }

    public static <T> T getBean(Class<T> clazz) {
        return CONTEXT_MANAGER.getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return CONTEXT_MANAGER.getBean(name, clazz);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return CONTEXT_MANAGER.getBeansOfType(clazz);
    }

    public static void setContext(Object context) {
        CONTEXT_MANAGER.setContext(context);
    }

    public static Object getEnvironment() {
        return CONTEXT_MANAGER.getEnvironment();
    }

    public static String getEnvironmentProperty(String key) {
        return CONTEXT_MANAGER.getEnvironmentProperty(key);
    }

    public static String getEnvironmentProperty(String key, String defaultValue) {
        return CONTEXT_MANAGER.getEnvironmentProperty(key, defaultValue);
    }

    public static String[] getActiveProfiles() {
        return CONTEXT_MANAGER.getActiveProfiles();
    }

    public static String[] getDefaultProfiles() {
        return CONTEXT_MANAGER.getDefaultProfiles();
    }

    public static void publishEvent(Object event) {
        EventBusManager.post(event);
    }
}

