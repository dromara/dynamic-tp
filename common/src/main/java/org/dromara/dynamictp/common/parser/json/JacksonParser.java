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

package org.dromara.dynamictp.common.parser.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author topsuder
 * @since 1.1.3
 */
@Slf4j
public class JacksonParser extends AbstractJsonParser {

    private static final String PACKAGE_NAME = "com.fasterxml.jackson.databind.ObjectMapper";

    private static final String JAVA_TIME_MODULE_CLASS_NAME = "com.fasterxml.jackson.datatype.jsr310.JavaTimeModule";

    private volatile ObjectMapper mapper;

    @Override
    public <T> T fromJson(String json, Type typeOfT) {
        try {
            final ObjectMapper objectMapper = getMapper();
            return objectMapper.readValue(json, objectMapper.constructType(typeOfT));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toJson(Object obj) {
        try {
            return getMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private ObjectMapper getMapper() {
        // double check lock
        if (mapper == null) {
            synchronized (this) {
                if (mapper == null) {
                    mapper = createMapper();
                }
            }
        }
        return mapper;
    }

    protected ObjectMapper createMapper() {
        return JacksonCreator.createMapper();
    }

    @Override
    protected String[] getMapperClassNames() {
        return new String[]{PACKAGE_NAME, JAVA_TIME_MODULE_CLASS_NAME};
    }
}
