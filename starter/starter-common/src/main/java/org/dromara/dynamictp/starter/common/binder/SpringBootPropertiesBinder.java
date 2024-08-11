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

package org.dromara.dynamictp.starter.common.binder;

import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.entity.DtpExecutorProps;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.spring.PropertiesBinder;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.MAIN_PROPERTIES_PREFIX;

/**
 * SpringBootPropertiesBinder related
 *
 * @author yanhom
 * @since 1.0.3
 **/
@Slf4j
@SuppressWarnings("all")
public class SpringBootPropertiesBinder implements PropertiesBinder {
    private static final String GLOBAL_PREFIX = "spring.dynamic.tp.globalExecutorProps.";
    private static final String EXECUTORS_PREFIX = "spring.dynamic.tp.executors[";

    @Override
    public void bindDtpProperties(Map<?, Object> properties, DtpProperties dtpProperties) {
        try {
            Class.forName("org.springframework.boot.context.properties.bind.Binder");
            doBindIn2X(properties, dtpProperties);
        } catch (ClassNotFoundException e) {
            doBindIn1X(properties, dtpProperties);
        }
        tryResetWithGlobalConfig(properties, dtpProperties);
    }

    @Override
    public void bindDtpProperties(Environment environment, DtpProperties dtpProperties) {
        try {
            Class.forName("org.springframework.boot.context.properties.bind.Binder");
            doBindIn2X(environment, dtpProperties);
        } catch (ClassNotFoundException e) {
            doBindIn1X(environment, dtpProperties);
        }
        tryResetWithGlobalConfig(environment,dtpProperties);
    }

    private void doBindIn2X(Map<?, Object> properties, DtpProperties dtpProperties) {
        ConfigurationPropertySource sources = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(sources);
        ResolvableType type = ResolvableType.forClass(DtpProperties.class);
        Bindable<?> target = Bindable.of(type).withExistingValue(dtpProperties);
        binder.bind(MAIN_PROPERTIES_PREFIX, target);
    }

    private void doBindIn2X(Environment environment, DtpProperties dtpProperties) {
        Binder binder = Binder.get(environment);
        ResolvableType type = ResolvableType.forClass(DtpProperties.class);
        Bindable<?> target = Bindable.of(type).withExistingValue(dtpProperties);
        binder.bind(MAIN_PROPERTIES_PREFIX, target);
    }

    private void doBindIn1X(Environment environment, DtpProperties dtpProperties) {
        try {
            // new RelaxedPropertyResolver(environment)
            Class<?> resolverClass = Class.forName("org.springframework.boot.bind.RelaxedPropertyResolver");
            Constructor<?> resolverConstructor = resolverClass.getDeclaredConstructor(PropertyResolver.class);
            Object resolver = resolverConstructor.newInstance(environment);

            // resolver.getSubProperties(MAIN_PROPERTIES_PREFIX)
            // return a map of all underlying properties that start with the specified key.
            // NOTE: this method can only be used if the underlying resolver is a ConfigurableEnvironment.
            Method getSubPropertiesMethod = resolverClass.getDeclaredMethod("getSubProperties", String.class);
            Map<?, ?> properties = (Map<?, ?>) getSubPropertiesMethod.invoke(resolver, StringUtils.EMPTY);

            doBindIn1X(properties, dtpProperties);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void doBindIn1X(Map<?, ?> properties, DtpProperties dtpProperties) {
        try {
            // new RelaxedDataBinder(dtpProperties, MAIN_PROPERTIES_PREFIX)
            Class<?> binderClass = Class.forName("org.springframework.boot.bind.RelaxedDataBinder");
            Constructor<?> binderConstructor = binderClass.getDeclaredConstructor(Object.class, String.class);
            Object binder = binderConstructor.newInstance(dtpProperties, MAIN_PROPERTIES_PREFIX);

            // binder.bind(new MutablePropertyValues(properties))
            Method bindMethod = binderClass.getMethod("bind", PropertyValues.class);
            bindMethod.invoke(binder, new MutablePropertyValues(properties));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Assign global environment variable to property
     * @param environment
     * @param dtpProperties
     */
    
    private void tryResetWithGlobalConfig(Object environment, DtpProperties dtpProperties) {
        final int[] i = {0};
        val fields = ReflectionUtil.getAllFields(DtpExecutorProps.class);
        if(CollectionUtils.isEmpty(fields)) {
            return;
        }
        dtpProperties.getExecutors().forEach(executor -> {
            fields.forEach(field -> {
                String globalFieldVal = "";
                String executorFieldVal = "";
                if(environment instanceof Environment) {
                    Environment env = (Environment) environment;
                    globalFieldVal = env.getProperty(GLOBAL_PREFIX + field.getName());
                }
                else if(environment instanceof Map) {
                    Map<?, Object> properties = (Map<?, Object>) environment;
                    Object globalPropertyField = properties.get(GLOBAL_PREFIX + field.getName());
                    Object executorPropertyField = properties.get(EXECUTORS_PREFIX + i[0] +"]." + field.getName());
                    if(globalPropertyField instanceof String) {
                        globalFieldVal = globalPropertyField.toString();
                        if(ObjectUtils.isNotEmpty(executorPropertyField)) {
                            executorFieldVal = executorPropertyField.toString();
                        }
                    }
                }
                if(StringUtils.isEmpty(globalFieldVal)) {
                    return;
                }
                if (StringUtils.isNotEmpty(executorFieldVal)) {
                    return;
                }
                ReflectUtil.setFieldValue(executor,field,globalFieldVal);
            });
            if (dtpProperties.getGlobalExecutorProps() != null) {
                Set<String> globalTaskWrapperNames = dtpProperties.getGlobalExecutorProps().getTaskWrapperNames();
                if(executor.getTaskWrapperNames() == null) {
                    executor.setTaskWrapperNames(globalTaskWrapperNames);
                }

                List<String> globalPlatformIds = dtpProperties.getGlobalExecutorProps().getPlatformIds();
                if(executor.getPlatformIds() == null) {
                    executor.setPlatformIds(globalPlatformIds);
                }

                List<NotifyItem> globalNotifyItems = dtpProperties.getGlobalExecutorProps().getNotifyItems();
                if(executor.getNotifyItems() == null) {
                    executor.setNotifyItems(globalNotifyItems);
                }
            }
            i[0]++;
        });
    }
}
