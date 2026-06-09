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

import org.dromara.dynamictp.spring.DtpBaseBeanConfiguration;
import org.dromara.dynamictp.spring.annotation.DtpBaseBeanDefinitionRegistrar;
import org.dromara.dynamictp.spring.annotation.DtpBeanDefinitionRegistrar;
import org.dromara.dynamictp.spring.annotation.DtpConfigurationSelector;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.mock.env.MockEnvironment;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.DTP_ENABLED_PROP;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DtpConfigurationSelectorTest {

    private final DtpConfigurationSelector selector = new DtpConfigurationSelector();

    @Test
    void shouldImportDynamicTpConfigurationByDefault() {
        selector.setEnvironment(new MockEnvironment());

        assertArrayEquals(new String[] {
                DtpBaseBeanDefinitionRegistrar.class.getName(),
                DtpBeanDefinitionRegistrar.class.getName(),
                DtpBaseBeanConfiguration.class.getName()
        }, selector.selectImports(null));
        assertEquals(Ordered.HIGHEST_PRECEDENCE, selector.getOrder());
    }

    @Test
    void shouldSkipImportsWhenDynamicTpIsDisabled() {
        selector.setEnvironment(new MockEnvironment().withProperty(DTP_ENABLED_PROP, "false"));

        assertArrayEquals(new String[0], selector.selectImports(null));
    }
}
