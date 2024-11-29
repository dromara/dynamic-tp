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

package org.dromara.dynamictp.test.common.em;

import org.dromara.dynamictp.common.em.JreEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;

/**
 * JreEnumTest related
 */
class JreEnumTest {

    @Test
    @EnabledOnJre(value = JRE.JAVA_8)
    void testJRE8() {
        Assertions.assertEquals(JreEnum.JAVA_8, JreEnum.currentVersion());
    }

    @Test
    @EnabledOnJre(value = JRE.JAVA_11)
    void testJRE11() {
        // 当前JRE版本为11，但是通过System.setProperty("java.version", "")设置为空情况
        System.setProperty("java.version", "");
        Assertions.assertEquals(JreEnum.JAVA_11, JreEnum.currentVersion());
    }

    @Test
    @EnabledOnJre(value = JRE.JAVA_11)
    void testJRE11GreaterThan() {
        Assertions.assertTrue(JreEnum.greaterThan(JreEnum.JAVA_8));
    }

    @Test
    @EnabledOnJre(value = JRE.JAVA_8)
    void testJRE8LessThan() {
        Assertions.assertTrue(JreEnum.lessThan(JreEnum.JAVA_11));
    }

}
