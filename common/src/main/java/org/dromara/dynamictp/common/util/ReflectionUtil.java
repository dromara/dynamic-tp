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

import lombok.val;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * ReflectionUtil related
 *
 * @author yanhom
 * @since 1.0.6
 */
public final class ReflectionUtil {

    private ReflectionUtil() { }

    public static Object getFieldValue(String fieldName, Object targetObj) {
        val field = getField(targetObj.getClass(), fieldName);
        if (Objects.isNull(field)) {
            return null;
        }
        try {
            val fieldObj = FieldUtils.readField(field, targetObj, true);
            return fieldObj;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public static Object getFieldValue(Class<?> targetClass, String fieldName, Object targetObj) {
        val field = getField(targetClass, fieldName);
        if (Objects.isNull(field)) {
            return null;
        }
        try {
            val fieldObj = FieldUtils.readField(field, targetObj, true);
            return fieldObj;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public static void setFieldValue(String fieldName, Object targetObj, Object targetVal) {
        val field = getField(targetObj.getClass(), fieldName);
        if (Objects.isNull(field)) {
            return;
        }
        try {
            FieldUtils.writeField(field, targetObj, targetVal, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void setFieldValue(Class<?> targetClass, String fieldName, Object targetObj, Object targetVal) {
        val field = getField(targetClass, fieldName);
        if (Objects.isNull(field)) {
            return;
        }
        try {
            FieldUtils.writeField(field, targetObj, targetVal, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Field getField(Class<?> targetClass, String fieldName) {
        Field field = FieldUtils.getField(targetClass, fieldName, true);
        if (Objects.isNull(field)) {
            return null;
        }
        return field;
    }
}
