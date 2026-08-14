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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

/**
 * Bean utility for copying properties between objects.
 * <p>
 * Uses fastjson2's {@link JSONObject#copyTo(Object)} under the hood.
 * {@link JSONWriter.Feature#WriteNulls} is enabled to preserve null-overwrite
 * semantics (matching the original hutool BeanUtil behavior).
 *
 * @author dynamic-tp
 * @since 1.2.3
 */
public final class BeanUtil {

    private BeanUtil() {
    }

    /**
     * Copy properties from source to target, including null values (overwriting target with null).
     *
     * @param source the source object
     * @param target the target object
     */
    public static void copyProperties(Object source, Object target) {
        copyProperties(source, target, true);
    }

    /**
     * Copy properties from source to target.
     *
     * @param source      the source object
     * @param target      the target object
     * @param writeNulls  if {@code true}, null property values in source will overwrite
     *                    the corresponding target properties (matching hutool BeanUtil behavior);
     *                    if {@code false}, null values are skipped
     */
    public static void copyProperties(Object source, Object target, boolean writeNulls) {
        JSONWriter.Feature[] features = writeNulls
                ? new JSONWriter.Feature[]{JSONWriter.Feature.WriteNulls}
                : new JSONWriter.Feature[]{};
        JSONObject jsonObj = (JSONObject) JSON.toJSON(source, features);
        jsonObj.copyTo(target);
    }
}
