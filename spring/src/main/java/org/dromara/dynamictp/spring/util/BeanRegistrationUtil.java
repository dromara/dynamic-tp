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

package org.dromara.dynamictp.spring.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.List;
import java.util.Map;

/**
 * SpringBeanHelper related
 *
 * @author yanhom
 * @since 1.0.4
 **/
@Slf4j
public final class BeanRegistrationUtil {

    private BeanRegistrationUtil() { }

    public static void register(BeanDefinitionRegistry registry,
                                String beanName,
                                Class<?> clazz,
                                Map<String, Object> propertyValues,
                                Object... constructorArgs) {
        register(registry, beanName, clazz, propertyValues, null, constructorArgs);
    }

    public static void register(BeanDefinitionRegistry registry,
                                String beanName,
                                Class<?> clazz,
                                Map<String, Object> propertyValues,
                                List<String> dependsOnBeanNames,
                                Object... constructorArgs) {
        if (ifPresent(registry, beanName, clazz) || registry.containsBeanDefinition(beanName)) {
            log.info("DynamicTp registrar, bean [{}] already exists and will be overwritten", beanName);
            registry.removeBeanDefinition(beanName);
        }
        doRegister(registry, beanName, clazz, propertyValues, dependsOnBeanNames, constructorArgs);
    }

    public static void registerIfAbsent(BeanDefinitionRegistry registry,
                                        String beanName,
                                        Class<?> clazz,
                                        Object... constructorArgs) {
        registerIfAbsent(registry, beanName, clazz, null, null, constructorArgs);
    }

    public static void registerIfAbsent(BeanDefinitionRegistry registry,
                                        String beanName,
                                        Class<?> clazz,
                                        Map<String, Object> propertyValues,
                                        Object... constructorArgs) {
        registerIfAbsent(registry, beanName, clazz, propertyValues, null, constructorArgs);
    }

    public static void registerIfAbsent(BeanDefinitionRegistry registry,
                                        String beanName,
                                        Class<?> clazz,
                                        Map<String, Object> propertyValues,
                                        List<String> dependsOnBeanNames,
                                        Object... constructorArgs) {
        if (!ifPresent(registry, beanName, clazz) && !registry.containsBeanDefinition(beanName)) {
            doRegister(registry, beanName, clazz, propertyValues, dependsOnBeanNames, constructorArgs);
        }
    }

    public static boolean ifPresent(BeanDefinitionRegistry registry, String beanName, Class<?> clazz) {
        String[] beanNames = getBeanNames((ListableBeanFactory) registry, clazz);
        return ArrayUtils.contains(beanNames, beanName);
    }

    public static String[] getBeanNames(ListableBeanFactory beanFactory, Class<?> clazz) {
        return beanFactory.getBeanNamesForType(clazz, true, false);
    }

    private static void doRegister(BeanDefinitionRegistry registry,
                                   String beanName,
                                   Class<?> clazz,
                                   Map<String, Object> propertyValues,
                                   List<String> dependsOnBeanNames,
                                   Object... constructorArgs) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        for (Object constructorArg : constructorArgs) {
            builder.addConstructorArgValue(constructorArg);
        }
        if (MapUtils.isNotEmpty(propertyValues)) {
            propertyValues.forEach(builder::addPropertyValue);
        }
        if (CollectionUtils.isNotEmpty(dependsOnBeanNames)) {
            dependsOnBeanNames.forEach(builder::addDependsOn);
        }
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }
}
