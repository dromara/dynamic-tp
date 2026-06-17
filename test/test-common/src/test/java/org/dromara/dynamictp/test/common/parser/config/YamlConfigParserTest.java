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

package org.dromara.dynamictp.test.common.parser.config;

import org.dromara.dynamictp.common.em.ConfigFileTypeEnum;
import org.dromara.dynamictp.common.parser.config.YamlConfigParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * YamlConfigParserTest related.
 */
class YamlConfigParserTest {

    @Test
    void testTypesAndSupports() {
        YamlConfigParser parser = new YamlConfigParser();

        Assertions.assertTrue(parser.supports(ConfigFileTypeEnum.YML));
        Assertions.assertTrue(parser.supports(ConfigFileTypeEnum.YAML));
        Assertions.assertFalse(parser.supports(ConfigFileTypeEnum.JSON));
        Assertions.assertEquals(2, parser.types().size());
    }

    @Test
    void testDoParseReturnsEmptyMapWhenContentIsBlank() {
        YamlConfigParser parser = new YamlConfigParser();

        Assertions.assertTrue(parser.doParse(" ").isEmpty());
    }

    @Test
    void testDoParseFlattensNestedObjectsAndArrays() {
        YamlConfigParser parser = new YamlConfigParser();
        String content = "dynamictp:\n"
                + "  enabled: true\n"
                + "  executors:\n"
                + "    - threadPoolName: order\n"
                + "      corePoolSize: 2\n";

        Map<Object, Object> result = parser.doParse(content);

        Assertions.assertEquals("true", result.get("dynamictp.enabled"));
        Assertions.assertEquals("order", result.get("dynamictp.executors[0].threadPoolName"));
        Assertions.assertEquals("2", result.get("dynamictp.executors[0].corePoolSize"));
    }
}
