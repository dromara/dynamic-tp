package org.dromara.dynamictp.common.util;

import net.sf.cglib.beans.BeanCopier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BeanCopierUtils related
 *
 * @author vzer200
 * @since 1.1.8
 */
public class BeanCopierUtils {
    private static final Map<String, BeanCopier> BEAN_COPIER_CACHE = new ConcurrentHashMap<>();

    public static BeanCopier getBeanCopier(Class<?> sourceClass, Class<?> targetClass) {
        String key = generateKey(sourceClass, targetClass);
        return BEAN_COPIER_CACHE.computeIfAbsent(key, k -> BeanCopier.create(sourceClass, targetClass, false));
    }

    private static String generateKey(Class<?> sourceClass, Class<?> targetClass) {
        return sourceClass.getName() + ":" + targetClass.getName();
    }

    public static void copyProperties(Object source, Object target) {
        BeanCopier copier = getBeanCopier(source.getClass(), target.getClass());
        copier.copy(source, target, null);
    }

}
