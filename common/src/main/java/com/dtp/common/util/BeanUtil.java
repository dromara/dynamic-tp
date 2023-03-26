package com.dtp.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.Map;

/**
 * BeanUtil related
 *
 * @author yanhom
 * @since 1.0.4
 **/
@Slf4j
public final class BeanUtil {

    private BeanUtil() { }

    public static void registerIfAbsent(BeanDefinitionRegistry registry,
                                        String beanName,
                                        Class<?> clazz,
                                        Map<String, Object> propertyValues,
                                        Object... constructorArgs) {
        if (ifPresent(registry, beanName, clazz) || registry.containsBeanDefinition(beanName)) {
            log.info("DynamicTp registrar, bean already exists and will be overwritten by remote config, beanName: {}",
                    beanName);
            registry.removeBeanDefinition(beanName);
        }
        doRegister(registry, beanName, clazz, propertyValues, constructorArgs);
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
                                  Map<String, Object> propertyValues,
                                  Object... constructorArgs) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        for (Object constructorArg : constructorArgs) {
            builder.addConstructorArgValue(constructorArg);
        }
        if (MapUtils.isNotEmpty(propertyValues)) {
            propertyValues.forEach(builder::addPropertyValue);
        }
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }
}
