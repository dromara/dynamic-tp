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

package org.dromara.dynamictp.common.util;

import org.dromara.dynamictp.common.parser.json.JsonParser;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * <p>JsonUtil 提供了将 Java 对象序列化为 JSON 字符串以及将 JSON 字符串反序列化为 Java 对象的方法。</p>
 * <p>JsonUtil 使用 SPI（Service Provider Interface）机制加载 JSON 序列化和反序列化器。</p>
 * <p>JsonUtil 可以自动检测并使用 Classpath 中可用的 JSON 序列化和反序列化器，包括 Jackson、Gson 和 FastJson。</p>
 * <p>如果在 Classpath 中找不到任何 JSON 序列化或反序列化器，则会抛出 IllegalStateException 异常。</p>
 * <p>注意：如果您的应用程序使用了多个 JSON 序列化或反序列化器，您需要在使用 JsonUtil 之前设置默认序列化器或传递正确的序列化器。</p>
 *
 * @author topsuder
 * @since 1.1.3
 */
@Slf4j
public final class JsonUtil {

    private static final JsonParser JSON_PARSER = createJsonParser();

    private static JsonParser createJsonParser() {
        ServiceLoader<JsonParser> serviceLoader = ServiceLoader.load(JsonParser.class);
        Iterator<JsonParser> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            try {
                JsonParser jsonParser = iterator.next();
                if (jsonParser.supports()) {
                    return jsonParser;
                }
            } catch (Throwable ignored) {
            }
        }
        throw new IllegalStateException("No JSON parser found");
    }

    /**
     * 方法注释: <br>
     * 〈可用于将任何 Java 值序列化为字符串的方法。〉
     *
     * @param obj 任意类型入参
     * @return java.lang.String
     * @author topsuder 🌼🐇
     */
    public static String toJson(Object obj) {
        return JSON_PARSER.toJson(obj);
    }

    /**
     * 方法注释: <br>
     * 〈此方法将指定的 Json 反序列化为指定类的对象。〉
     *
     * @param <T>     the target type
     * @param json    要反序列化的json字符串
     * @param typeOfT 要反序列化的对象类型
     * @return T
     * @author topsuder 🌼🐇
     */
    public static <T> T fromJson(String json, Type typeOfT) {
        return JSON_PARSER.fromJson(json, typeOfT);
    }
}
