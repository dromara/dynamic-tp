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

import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages event registration and posting using EventBus.
 *
 * @author vzer200
 * @since 1.2.0
 */
@Slf4j
public class EventBusManager {

    private static final EventBus EVENT_BUS = new EventBus();

    private static final Set<Object> REGISTERED_OBJECTS = ConcurrentHashMap.newKeySet();

    private EventBusManager() { }

    public static void register(Object object) {
        if (REGISTERED_OBJECTS.add(object)) {
            EVENT_BUS.register(object);
        }
    }

    public static void unregister(Object object) {
        if (REGISTERED_OBJECTS.remove(object)) {
            try {
                EVENT_BUS.unregister(object);
            } catch (IllegalArgumentException e) {
                // log warning or handle the case where the object is not registered
                log.warn("Attempted to unregister an object that was not registered: {}", object, e);
            }
        }
    }

    public static void post(Object event) {
        EVENT_BUS.post(event);
    }

    public static EventBus getInstance() {
        return EVENT_BUS;
    }

    public static void destroy() {
        for (Object object : REGISTERED_OBJECTS) {
            try {
                EVENT_BUS.unregister(object);
            } catch (Exception e) {
                log.warn("Attempted to unregister an object that was not registered: {}", object, e);
            }
        }
        REGISTERED_OBJECTS.clear();
    }
}

