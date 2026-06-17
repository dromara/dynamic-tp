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

import org.dromara.dynamictp.common.util.UUIDUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * UUIDUtilTest related.
 */
class UUIDUtilTest {

    @Test
    void testGenUuidReturnsStandardUuid() {
        String uuid = UUIDUtil.genUuid();

        Assertions.assertEquals(36, uuid.length());
        Assertions.assertTrue(uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    void testGenUuidWithLengthReturnsTrimmedUuidWithoutHyphen() {
        String uuid = UUIDUtil.genUuid(16);

        Assertions.assertEquals(16, uuid.length());
        Assertions.assertFalse(uuid.contains("-"));
        Assertions.assertTrue(uuid.matches("[0-9a-f]{16}"));
    }
}
