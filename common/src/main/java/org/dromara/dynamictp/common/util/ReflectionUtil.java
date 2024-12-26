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

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.List;
import java.util.Objects;

/**
 * ReflectionUtil related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
public final class ReflectionUtil {

    private ReflectionUtil() { }

    public static Object getFieldValue(String fieldName, Object targetObj) {
        val field = getField(targetObj.getClass(), fieldName);
        if (Objects.isNull(field)) {
            return null;
        }
        try {
            return FieldUtils.readField(field, targetObj, true);
        } catch (IllegalAccessException e) {
            log.error("Failed to read field '{}' from object '{}'", fieldName, targetObj, e);
            return null;
        }
    }

    public static Object getFieldValue(Class<?> targetClass, String fieldName, Object targetObj) {
        val field = getField(targetClass, fieldName);
        if (Objects.isNull(field)) {
            return null;
        }
        try {
            return FieldUtils.readField(field, targetObj, true);
        } catch (IllegalAccessException e) {
            log.error("Failed to read field '{}' from object '{}'", fieldName, targetObj, e);
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
            log.error("Failed to write value '{}' to field '{}' in object '{}'", targetVal, fieldName, targetObj, e);
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
            log.error("Failed to write value '{}' to field '{}' in object '{}'", targetVal, fieldName, targetObj, e);
        }
    }

    public static Field getField(Class<?> targetClass, String fieldName) {
        Field field = FieldUtils.getField(targetClass, fieldName, true);
        if (Objects.isNull(field)) {
            log.warn("Field '{}' not found in class '{}'", fieldName, targetClass.getName());
            return null;
        }
        return field;
    }

    public static Method findMethod(Class<?> targetClass, String methodName, Class<?>... parameterTypes) {
        Method method = MethodUtils.getMatchingMethod(targetClass, methodName, parameterTypes);
        if (Objects.isNull(method)) {
            log.warn("Method '{}' with parameters '{}' not found in class '{}'", methodName, parameterTypes, targetClass.getName());
        }
        return method;
    }

    public static List<Field> getAllFields(Class<?> targetClass) {
        return FieldUtils.getAllFieldsList(targetClass);
    }
}
