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

package org.dromara.dynamictp.test.common.util;

import net.sf.cglib.beans.BeanCopier;
import org.dromara.dynamictp.common.util.BeanCopierUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * BeanCopierUtilsTest related
 *
 * @author vzer200
 * @since 1.1.8
 */
public class BeanCopierUtilTest {

    private SourceClass source;
    private TargetClass target;

    @BeforeEach
    public void setUp() {
        source = new SourceClass();
        source.setId(1);
        source.setName("Test Name");
        source.setValue(100);

        target = new TargetClass();
    }

    @Test
    public void testCopyProperties() {
        // 使用BeanCopierUtils复制属性
        BeanCopierUtil.copyProperties(source, target);

        // 验证目标对象的属性值是否正确复制
        assertEquals(source.getId(), target.getId());
        assertEquals(source.getName(), target.getName());
        assertEquals(source.getValue(), target.getValue());
    }

    @Test
    public void testCopyPropertiesWithNullValues() {
        // 测试当源对象中有空值时的情况
        source.setName(null);

        BeanCopierUtil.copyProperties(source, target);

        assertEquals(source.getId(), target.getId());
        assertNull(target.getName()); // 名称为空时应正确复制
        assertEquals(source.getValue(), target.getValue());
    }

    @Test
    public void testBeanCopierCache() {
        // 测试BeanCopier缓存机制是否有效
        BeanCopier firstCopier = BeanCopierUtil.getBeanCopier(SourceClass.class, TargetClass.class);
        BeanCopier secondCopier = BeanCopierUtil.getBeanCopier(SourceClass.class, TargetClass.class);

        assertSame(firstCopier, secondCopier); // 同样的source和target类应返回同一个BeanCopier实例
    }

    // 示例的源类和目标类
    public static class SourceClass {
        private int id;
        private String name;
        private int value;

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    public static class TargetClass {
        private int id;
        private String name;
        private int value;

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }
}