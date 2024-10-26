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

import org.dromara.dynamictp.spring.holder.SpringContextHolder;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * SpringContextHolderTest related
 *
 * @author vzer200
 * @since 1.1.8
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 每个测试类使用同一个实例
public class SpringContextHolderTest {

    private ApplicationContext mockContext;
    private Environment mockEnv;

    @BeforeEach
    void setUp() throws Exception {
        // 每个测试都创建新的 mock 对象
        mockContext = Mockito.mock(ApplicationContext.class);
        mockEnv = Mockito.mock(Environment.class);

        // 配置 mock 行为
        when(mockContext.getEnvironment()).thenReturn(mockEnv);

        // 使用反射设置 SpringContextHolder 的静态 context
        setStaticContext(mockContext);
    }

    private void setStaticContext(ApplicationContext context) throws Exception {
        Field contextField = SpringContextHolder.class.getDeclaredField("context");
        contextField.setAccessible(true);
        contextField.set(null, context);
    }

    @Test
    public void testGetBeanByClass() {
        String expectedBean = "testBean";
        when(mockContext.getBean(String.class)).thenReturn(expectedBean);

        String actualBean = SpringContextHolder.getInstance().getBean(String.class);

        assertEquals(expectedBean, actualBean);
    }

    @Test
    public void testGetBeanByNameAndClass() {
        String expectedBean = "testBean";
        when(mockContext.getBean("beanName", String.class)).thenReturn(expectedBean);

        String actualBean = SpringContextHolder.getInstance().getBean("beanName", String.class);

        assertEquals(expectedBean, actualBean);
    }

    @Test
    public void testGetBeansOfType() {
        Map<String, String> expectedBeans = new HashMap<>();
        expectedBeans.put("bean1", "value1");
        expectedBeans.put("bean2", "value2");

        when(mockContext.getBeansOfType(String.class)).thenReturn(expectedBeans);

        Map<String, String> actualBeans = SpringContextHolder.getInstance().getBeansOfType(String.class);

        assertEquals(expectedBeans, actualBeans);
    }

    @Test
    public void testGetEnvironmentProperty() {
        when(mockEnv.getProperty("key")).thenReturn("value");

        String actualProperty = SpringContextHolder.getInstance().getEnvironment().getProperty("key");

        assertEquals("value", actualProperty);
    }

    @Test
    public void testGetEnvironmentPropertyWithDefaultValue() {
        when(mockEnv.getProperty("key", "default")).thenReturn("value");

        String actualProperty = SpringContextHolder.getInstance().getEnvironment().getProperty("key", "default");

        assertEquals("value", actualProperty);
    }

    @Test
    public void testGetActiveProfiles() {
        when(mockEnv.getActiveProfiles()).thenReturn(new String[]{"profile1", "profile2"});

        String[] actualProfiles = SpringContextHolder.getInstance().getEnvironment().getActiveProfiles();

        assertArrayEquals(new String[]{"profile1", "profile2"}, actualProfiles);
    }
}
