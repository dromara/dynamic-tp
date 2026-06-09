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

package org.dromara.dynamictp.test.core.spring;

import org.dromara.dynamictp.spring.initializer.SpringDtpInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.APP_ENV_KEY;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.APP_NAME_KEY;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.APP_PORT_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import static org.junit.jupiter.api.parallel.Resources.SYSTEM_PROPERTIES;

/**
 * SpringDtpInitializerTest related
 *
 * @author yanhom
 * @since 1.2.2
 */
@Execution(SAME_THREAD)
@ResourceLock(SYSTEM_PROPERTIES)
class SpringDtpInitializerTest {

    private final SpringDtpInitializer initializer = new SpringDtpInitializer();

    @AfterEach
    void clearProperties() {
        System.clearProperty(APP_NAME_KEY);
        System.clearProperty(APP_PORT_KEY);
        System.clearProperty(APP_ENV_KEY);
    }

    @Test
    void shouldInitializeApplicationPropertiesFromEnvironment() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.application.name", "demo")
                .withProperty("server.port", "8080")
                .withProperty("spring.profiles.active", "prod");

        initializer.init(contextWith(environment));

        assertEquals("SpringDtpInitializer", initializer.getName());
        assertEquals("demo", System.getProperty(APP_NAME_KEY));
        assertEquals("8080", System.getProperty(APP_PORT_KEY));
        assertEquals("prod", System.getProperty(APP_ENV_KEY));
    }

    @Test
    void shouldFallbackToActiveThenDefaultProfile() {
        MockEnvironment activeEnvironment = new MockEnvironment();
        activeEnvironment.setActiveProfiles("test");
        initializer.init(contextWith(activeEnvironment));
        assertEquals("test", System.getProperty(APP_ENV_KEY));

        MockEnvironment defaultEnvironment = new MockEnvironment();
        defaultEnvironment.setDefaultProfiles("local");
        initializer.init(contextWith(defaultEnvironment));
        assertEquals("local", System.getProperty(APP_ENV_KEY));
    }

    @Test
    void shouldUseDefaultsWhenApplicationPropertiesAreMissing() {
        MockEnvironment environment = new MockEnvironment();
        environment.setDefaultProfiles();

        initializer.init(contextWith(environment));

        assertEquals("application", System.getProperty(APP_NAME_KEY));
        assertEquals("0", System.getProperty(APP_PORT_KEY));
        assertEquals("unknown", System.getProperty(APP_ENV_KEY));
    }

    private GenericApplicationContext contextWith(MockEnvironment environment) {
        GenericApplicationContext context = new GenericApplicationContext();
        context.setEnvironment(environment);
        return context;
    }
}
