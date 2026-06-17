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

package org.dromara.dynamictp.test.common.parser.json;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dromara.dynamictp.common.parser.json.AbstractJsonParser;
import org.dromara.dynamictp.common.parser.json.FastJsonParser;
import org.dromara.dynamictp.common.parser.json.GsonParser;
import org.dromara.dynamictp.common.parser.json.JacksonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

/**
 * JsonParserTest related.
 */
class JsonParserTest {

    @Test
    void testSupportsReturnsTrueWhenMapperClassesExist() {
        Assertions.assertTrue(new FastJsonParser().supports());
        Assertions.assertTrue(new GsonParser().supports());
    }

    @Test
    void testJacksonParserSupportReflectsOptionalJsr310Dependency() {
        boolean jsr310Available = isClassPresent("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule");

        Assertions.assertEquals(jsr310Available, new JacksonParser().supports());
    }

    @Test
    void testSupportsReturnsFalseWhenMapperClassMissing() {
        AbstractJsonParser parser = new MissingClassParser();

        Assertions.assertFalse(parser.supports());
    }

    @Test
    void testGsonParserSerializesAndDeserializesNullLocalDateTime() {
        GsonParser parser = new GsonParser();
        JsonPayload payload = new JsonPayload("demo", null);

        String json = parser.toJson(payload);
        JsonPayload result = parser.fromJson(json, JsonPayload.class);

        Assertions.assertFalse(json.contains("time"));
        Assertions.assertEquals(payload, result);
    }

    @Test
    void testGsonParserSerializesAndDeserializesLocalDateTime() {
        GsonParser parser = new GsonParser();
        JsonPayload payload = new JsonPayload("demo", LocalDateTime.of(2026, 6, 3, 10, 20, 30));

        String json = parser.toJson(payload);
        JsonPayload result = parser.fromJson(json, JsonPayload.class);

        Assertions.assertTrue(json.contains("\"2026-06-03 10:20:30\""));
        Assertions.assertEquals(payload, result);
    }

    @Test
    void testJacksonParserWrapsInvalidJson() {
        org.junit.jupiter.api.Assumptions.assumeTrue(
                isClassPresent("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule"));
        JacksonParser parser = new JacksonParser();

        Assertions.assertThrows(RuntimeException.class, () -> parser.fromJson("{invalid", JsonPayload.class));
    }

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static class MissingClassParser extends AbstractJsonParser {

        @Override
        public <T> T fromJson(String json, Type typeOfT) {
            return null;
        }

        @Override
        public String toJson(Object obj) {
            return null;
        }

        @Override
        protected String[] getMapperClassNames() {
            return new String[]{"org.dromara.dynamictp.test.MissingJsonMapper"};
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class JsonPayload {

        private String name;

        private LocalDateTime time;
    }
}
