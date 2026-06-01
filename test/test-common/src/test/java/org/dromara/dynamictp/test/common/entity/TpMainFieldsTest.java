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

package org.dromara.dynamictp.test.common.entity;

import org.dromara.dynamictp.common.entity.TpMainFields;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TpMainFieldsTest related.
 */
class TpMainFieldsTest {

    @Test
    void testGetMainFieldsReturnsDeclaredFields() {
        List<String> fieldNames = TpMainFields.getMainFields().stream()
                .map(Field::getName)
                .collect(Collectors.toList());

        Assertions.assertTrue(fieldNames.contains("FIELD_NAMES"));
        Assertions.assertTrue(fieldNames.contains("threadPoolName"));
        Assertions.assertTrue(fieldNames.contains("corePoolSize"));
        Assertions.assertTrue(fieldNames.contains("maxPoolSize"));
        Assertions.assertTrue(fieldNames.contains("keepAliveTime"));
        Assertions.assertTrue(fieldNames.contains("queueType"));
        Assertions.assertTrue(fieldNames.contains("queueCapacity"));
        Assertions.assertTrue(fieldNames.contains("rejectType"));
        Assertions.assertTrue(fieldNames.contains("allowCoreThreadTimeOut"));
    }
}
