package com.dtp.common.util;

import lombok.val;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * ReflectionUtil related
 *
 * @author yanhom
 * @since 1.0.6
 */
public class ReflectionUtil {

    private ReflectionUtil() {}

    public static Object getField(Class<?> targetClass, String fieldName, Object targetObj) {
        Field field = ReflectionUtils.findField(targetClass, fieldName);
        if (Objects.isNull(field)) {
            return null;
        }
        ReflectionUtils.makeAccessible(field);
        val fieldObj = ReflectionUtils.getField(field, targetObj);
        if (Objects.isNull(fieldObj)) {
            return null;
        }
        return fieldObj;
    }
}
