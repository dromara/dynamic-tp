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
import org.dromara.dynamictp.common.entity.DtpExecutorProps;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
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
        val fieldObj = ReflectionUtils.getField(field, targetObj);
        if (Objects.isNull(fieldObj)) {
            return null;
        }
        return fieldObj;
    }

    public static Object getFieldValue(Class<?> targetClass, String fieldName, Object targetObj) {
        val field = getField(targetClass, fieldName);
        if (Objects.isNull(field)) {
            return null;
        }
        val fieldObj = ReflectionUtils.getField(field, targetObj);
        if (Objects.isNull(fieldObj)) {
            return null;
        }
        return fieldObj;
    }

    public static void setFieldValue(String fieldName, Object targetObj, Object targetVal)
            throws IllegalAccessException {
        val field = getField(targetObj.getClass(), fieldName);
        if (Objects.isNull(field)) {
            return;
        }
        field.set(targetObj, targetVal);
    }


    public static void setFieldValue(Class<?> targetClass, String fieldName, Object targetObj, Object targetVal)
            throws IllegalAccessException {
        val field = getField(targetClass, fieldName);
        if (Objects.isNull(field)) {
            return;
        }
        field.set(targetObj, targetVal);
    }

    public static Field getField(Class<?> targetClass, String fieldName) {
        Field field = ReflectionUtils.findField(targetClass, fieldName);
        if (Objects.isNull(field)) {
            return null;
        }
        ReflectionUtils.makeAccessible(field);
        return field;
    }

    public static List<Field> getAllFields(Class<DtpExecutorProps> dtpExecutorPropsClass) {
        List<Field> fields =new ArrayList<Field>();
        ReflectionUtils.doWithFields(dtpExecutorPropsClass, field->{
            fields.add(field);
        });
        return fields;
    }

    public static void setGlobalFieldValue(Class<?> targetClass, String fieldName, DtpExecutorProps targetObj, String targetVal)
            throws IllegalAccessException{
        val field = getField(targetClass, fieldName);
        if (Objects.isNull(field)) {
            return;
        }
        if(targetVal=="true"||targetVal=="false"){
            field.set(targetObj,Boolean.parseBoolean(targetVal));
            return;
        }
        boolean flag=true;
        for (int i = 0; i < targetVal.length(); i++) {
            if(!('0'<=targetVal.charAt(i)&&targetVal.charAt(i)<='9')){
                flag=false;
            }
        }
        if(flag==false) field.set(targetObj, targetVal);
        else field.set(targetObj,Integer.parseInt(targetVal));
    }
}
