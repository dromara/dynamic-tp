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

import org.dromara.dynamictp.common.util.StringUtil;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * StringUtilTest related
 *
 * @author yanhom
 * @date 2022-11-20 8:16 PM
 */
class StringUtilTest {

    @Test
    void testContainsIgnoreCase() {
        String str = "ttl";
        List<String> testStrList = Lists.newArrayList("ttl", "mdc");
        boolean r0 = StringUtil.containsIgnoreCase(str, testStrList);
        Assertions.assertTrue(r0);

        boolean r = StringUtil.containsIgnoreCase("str", testStrList);
        Assertions.assertFalse(r);
    }
}
