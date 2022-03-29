package com.dtp.common.util;

import cn.hutool.core.collection.CollUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.Map;

/**
 * BeanUtil related
 *
 * @author: yanhom
 * @since 1.0.4
 **/
public class BeanUtil {

    public static void registerIfAbsent(BeanDefinitionRegistry registry,
                                        String beanName,
                                        Class<?> clazz,
                                        Map<String, Object> properties,
                                        Object... constructorArgs) {
        if (ifPresent(registry, beanName, clazz) || registry.containsBeanDefinition(beanName)) {
            return;
        }
        doRegister(registry, beanName, clazz, properties, constructorArgs);
    }

    public static boolean ifPresent(BeanDefinitionRegistry registry, String beanName, Class<?> clazz) {
        String[] beanNames = getBeanNames((ListableBeanFactory) registry, clazz);
        return ArrayUtils.contains(beanNames, beanName);
    }

    public static String[] getBeanNames(ListableBeanFactory beanFactory, Class<?> clazz) {
        return beanFactory.getBeanNamesForType(clazz, true, false);
    }

    public static void doRegister(BeanDefinitionRegistry registry,
                                  String beanName,
                                  Class<?> clazz,
                                  Map<String, Object> properties,
                                  Object... constructorArgs) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        for (Object constructorArg : constructorArgs) {
            builder.addConstructorArgValue(constructorArg);
        }
        if (CollUtil.isNotEmpty(properties)) {
            properties.forEach(builder::addPropertyValue);
        }

        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }
}
