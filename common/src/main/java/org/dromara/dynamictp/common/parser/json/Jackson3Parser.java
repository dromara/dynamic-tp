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

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;

/**
 * Jackson 3 JSON parser.
 *
 * @author yanhom
 * @since 4.0.0
 */
public class Jackson3Parser extends AbstractJsonParser {

    private static final String PACKAGE_NAME = "tools.jackson.databind.ObjectMapper";

    private volatile ObjectMapper mapper;

    @Override
    public <T> T fromJson(String json, Type typeOfT) {
        try {
            ObjectMapper objectMapper = getMapper();
            return objectMapper.readValue(json, objectMapper.constructType(typeOfT));
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toJson(Object obj) {
        try {
            return getMapper().writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    private ObjectMapper getMapper() {
        if (mapper == null) {
            synchronized (this) {
                if (mapper == null) {
                    mapper = Jackson3Creator.createMapper();
                }
            }
        }
        return mapper;
    }

    @Override
    protected String[] getMapperClassNames() {
        return new String[] {PACKAGE_NAME};
    }
}
