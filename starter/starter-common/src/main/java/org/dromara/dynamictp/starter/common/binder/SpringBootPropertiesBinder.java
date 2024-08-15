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
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.entity.DtpExecutorProps;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.support.PropertiesBinder;
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
import java.util.Map;
import java.util.Objects;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.EXECUTORS_CONFIG_PREFIX;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.GLOBAL_CONFIG_PREFIX;
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

    @Override
    public void bindDtpProperties(Map<?, Object> properties, DtpProperties dtpProperties) {
        beforeBind(properties, dtpProperties);
        try {
            Class.forName("org.springframework.boot.context.properties.bind.Binder");
            doBindIn2X(properties, dtpProperties);
        } catch (ClassNotFoundException e) {
            doBindIn1X(properties, dtpProperties);
        }
        afterBind(properties, dtpProperties);
    }

    @Override
    public void bindDtpProperties(Object environment, DtpProperties dtpProperties) {
        if (!(environment instanceof Environment)) {
            throw new IllegalArgumentException("Invalid environment type, expected org.springframework.core.env.Environment");
        }
        Environment env = (Environment) environment;

        beforeBind(env, dtpProperties);
        try {
            Class.forName("org.springframework.boot.context.properties.bind.Binder");
            doBindIn2X(env, dtpProperties);
        } catch (ClassNotFoundException e) {
            doBindIn1X(env, dtpProperties);
        }
        afterBind(environment, dtpProperties);
    }

    @Override
    public void afterBind(Object source, DtpProperties dtpProperties) {
        tryResetWithGlobalConfig(source, dtpProperties);
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
     *
     * @param environment
     * @param dtpProperties
     */
    private void tryResetWithGlobalConfig(Object source, DtpProperties dtpProperties) {
        if (Objects.isNull(dtpProperties.getGlobalExecutorProps()) ||
                CollectionUtils.isEmpty(dtpProperties.getExecutors())) {
            return;
        }
        val fields = ReflectionUtil.getAllFields(DtpExecutorProps.class);
        if (CollectionUtils.isEmpty(fields)) {
            return;
        }

        final int[] executorIndex = {0};
        dtpProperties.getExecutors().forEach(executor -> {
            fields.forEach(field -> {
                Object executorFieldVal = getProperty(EXECUTORS_CONFIG_PREFIX + executorIndex[0] + "]." + field.getName(), source);
                if (Objects.nonNull(executorFieldVal)) {
                    return;
                }
                Object globalFieldVal = getProperty(GLOBAL_CONFIG_PREFIX + field.getName(), source);
                if (Objects.isNull(globalFieldVal)) {
                    return;
                }
                ReflectUtil.setFieldValue(executor, field, globalFieldVal);
            });

            val globalExecutorProps = dtpProperties.getGlobalExecutorProps();
            if (CollectionUtils.isEmpty(executor.getTaskWrapperNames()) &&
                    CollectionUtils.isNotEmpty(globalExecutorProps.getTaskWrapperNames())) {
                executor.setTaskWrapperNames(globalExecutorProps.getTaskWrapperNames());
            }
            if (CollectionUtils.isEmpty(executor.getPlatformIds()) &&
                    CollectionUtils.isNotEmpty(globalExecutorProps.getPlatformIds())) {
                executor.setPlatformIds(globalExecutorProps.getPlatformIds());
            }
            if (CollectionUtils.isEmpty(executor.getNotifyItems()) &&
                    CollectionUtils.isNotEmpty(globalExecutorProps.getNotifyItems())) {
                executor.setNotifyItems(globalExecutorProps.getNotifyItems());
            }
            if (CollectionUtils.isEmpty(executor.getAwareNames()) &&
                    CollectionUtils.isNotEmpty(globalExecutorProps.getAwareNames())) {
                executor.setAwareNames(globalExecutorProps.getAwareNames());
            }
            if (CollectionUtils.isEmpty(executor.getPluginNames()) &&
                    CollectionUtils.isNotEmpty(globalExecutorProps.getPluginNames())) {
                executor.setPluginNames(globalExecutorProps.getPluginNames());
            }
            executorIndex[0]++;
        });
    }

    private Object getProperty(String key, Object environment) {
        if (environment instanceof Environment) {
            Environment env = (Environment) environment;
            return env.getProperty(key);
        } else if (environment instanceof Map) {
            Map<?, Object> properties = (Map<?, Object>) environment;
            return properties.get(key);
        }
        return null;
    }
}
