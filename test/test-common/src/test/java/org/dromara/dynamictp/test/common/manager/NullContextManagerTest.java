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

package org.dromara.dynamictp.test.common.manager;

import org.dromara.dynamictp.common.manager.NullContextManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * NullContextManagerTest related.
 */
class NullContextManagerTest {

    @Test
    void testNullContextManagerReturnsEmptyValues() {
        NullContextManager contextManager = new NullContextManager();

        Assertions.assertNull(contextManager.getBean(Object.class));
        Assertions.assertNull(contextManager.getBean("bean", Object.class));
        Assertions.assertTrue(contextManager.getBeansOfType(Object.class).isEmpty());
        Assertions.assertNull(contextManager.getEnvironment());
        Assertions.assertNull(contextManager.getEnvironmentProperty("key"));
        Assertions.assertNull(contextManager.getEnvironmentProperty("key", new Object()));
        Assertions.assertNull(contextManager.getEnvironmentProperty("key", "default"));
    }
}
