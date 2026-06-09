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

import org.dromara.dynamictp.spring.util.BeanRegistrationUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeanRegistrationUtilTest {

    @Test
    void shouldRegisterBeanWithConstructorPropertiesAndDependencies() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("dependency", new Object());

        BeanRegistrationUtil.registerIfAbsent(beanFactory, "sample", SampleBean.class,
                Collections.singletonMap("name", "configured"),
                Collections.singletonList("dependency"), 7);

        SampleBean bean = beanFactory.getBean("sample", SampleBean.class);
        assertEquals(7, bean.number);
        assertEquals("configured", bean.name);
        assertArrayEquals(new String[] {"dependency"},
                beanFactory.getBeanDefinition("sample").getDependsOn());
        assertTrue(BeanRegistrationUtil.ifPresent(beanFactory, "sample", SampleBean.class));
        assertArrayEquals(new String[] {"sample"},
                BeanRegistrationUtil.getBeanNames(beanFactory, SampleBean.class));
    }

    @Test
    void shouldKeepExistingBeanWhenRegisteringIfAbsentAndReplaceOnRegister() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        BeanRegistrationUtil.registerIfAbsent(beanFactory, "sample", SampleBean.class, 1);
        SampleBean original = beanFactory.getBean("sample", SampleBean.class);

        BeanRegistrationUtil.registerIfAbsent(beanFactory, "sample", SampleBean.class, 2);
        assertSame(original, beanFactory.getBean("sample", SampleBean.class));

        beanFactory.destroySingleton("sample");
        BeanRegistrationUtil.register(beanFactory, "sample", SampleBean.class, null, 3);
        assertEquals(3, beanFactory.getBean("sample", SampleBean.class).number);
    }

    public static class SampleBean {

        private final int number;
        private String name;

        public SampleBean(int number) {
            this.number = number;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
