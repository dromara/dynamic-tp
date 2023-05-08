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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 *
 * @author topsuder
 * @since 1.1.3
 */
public class GsonParser extends AbstractJsonParser {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String PACKAGE_NAME = "com.google.gson.Gson";

    private volatile Gson mapper;

    @Override
    public <T> T fromJson(String json, Type typeOfT) {
        return getMapper().fromJson(json, typeOfT);
    }

    @Override
    public String toJson(Object obj) {
        return getMapper().toJson(obj);
    }

    private Gson getMapper() {
        if (mapper == null) {
            synchronized (this) {
                if (mapper == null) {
                    mapper = createMapper();
                }
            }
        }
        return mapper;
    }

    protected Gson createMapper() {
        TypeAdapter<LocalDateTime> timeTypeAdapter = new TypeAdapter<LocalDateTime>() {
            @Override
            public void write(JsonWriter out, LocalDateTime value) throws IOException {
                if (Objects.nonNull(value)) {
                    out.value(value.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
                } else {
                    out.nullValue();
                }
            }

            @Override
            public LocalDateTime read(JsonReader in) throws IOException {
                final JsonToken token = in.peek();
                if (token == JsonToken.NULL) {
                    return null;
                } else {
                    return LocalDateTime.parse(in.nextString(), DateTimeFormatter.ofPattern(DATE_FORMAT));
                }
            }
        };

        return new GsonBuilder()
                .setDateFormat(DATE_FORMAT)
                .registerTypeAdapter(LocalDateTime.class, timeTypeAdapter)
                .create();
    }

    @Override
    protected String getMapperClassName() {
        return PACKAGE_NAME;
    }
}
