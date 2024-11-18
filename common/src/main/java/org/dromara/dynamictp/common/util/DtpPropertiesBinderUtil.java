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

import cn.hutool.core.util.ReflectUtil;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.entity.DtpExecutorProps;
import org.dromara.dynamictp.common.entity.TpExecutorProps;
import org.dromara.dynamictp.common.manager.ContextManagerHelper;
import org.dromara.dynamictp.common.properties.DtpProperties;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.AWARE_NAMES;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.EXECUTORS_CONFIG_PREFIX;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.GLOBAL_CONFIG_PREFIX;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.MAIN_PROPERTIES_PREFIX;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.NOTIFY_ITEMS;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.PLATFORM_IDS;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.PLUGIN_NAMES;

/**
 * DtpPropertiesBinderUtil related
 *
 * @author yanhom
 * @since 1.1.9
 */
@SuppressWarnings("unchecked")
public final class DtpPropertiesBinderUtil {

    private DtpPropertiesBinderUtil() {
    }

    /**
     * Assign global environment variable to property
     *
     * @param source        environment
     * @param dtpProperties dtpProperties
     */
    public static void tryResetWithGlobalConfig(Object source, DtpProperties dtpProperties) {
        if (Objects.isNull(dtpProperties.getGlobalExecutorProps())) {
            return;
        }
        if (CollectionUtils.isNotEmpty(dtpProperties.getExecutors())) {
            tryResetCusExecutors(dtpProperties, source);
        }
        tryResetAdapterExecutors(dtpProperties, source);
    }

    private static void tryResetCusExecutors(DtpProperties dtpProperties, Object source) {
        val dtpPropsFields = ReflectionUtil.getAllFields(DtpExecutorProps.class);
        val globalExecutorProps = dtpProperties.getGlobalExecutorProps();
        int[] idx = {0};
        dtpProperties.getExecutors().forEach(executor -> {
            dtpPropsFields.forEach(field -> {
                String propKey = EXECUTORS_CONFIG_PREFIX + idx[0] + "]." + field.getName();
                setBasicField(source, field, executor, propKey);
            });
            String executorFieldNamePrefix = EXECUTORS_CONFIG_PREFIX + idx[0] + "]";
            setCollectionField(source, globalExecutorProps, executor, executorFieldNamePrefix);
            idx[0]++;
        });
    }

    private static void tryResetAdapterExecutors(DtpProperties dtpProperties, Object source) {
        val dtpPropertiesFields = ReflectionUtil.getAllFields(DtpProperties.class);
        val tpExecutorPropFields = ReflectionUtil.getAllFields(TpExecutorProps.class);
        val globalExecutorProps = dtpProperties.getGlobalExecutorProps();
        dtpPropertiesFields.forEach(dtpPropertiesField -> {
            val candidateExecutor = ReflectUtil.getFieldValue(dtpProperties, dtpPropertiesField);
            if (Objects.isNull(candidateExecutor)) {
                return;
            }
            String candidateExecutorFieldName = dtpPropertiesField.getName();
            if (dtpPropertiesField.getType().isAssignableFrom(TpExecutorProps.class)) {
                tpExecutorPropFields.forEach(field -> setBasicField(source, field, candidateExecutorFieldName, candidateExecutor));
                String executorFieldNamePrefix = MAIN_PROPERTIES_PREFIX + "." + dtpPropertiesField.getName();
                setCollectionField(source, globalExecutorProps, candidateExecutor, executorFieldNamePrefix);
            } else if (dtpPropertiesField.getGenericType() instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) dtpPropertiesField.getGenericType();
                Type[] argTypes = paramType.getActualTypeArguments();
                if (argTypes.length == 1 && argTypes[0].equals(TpExecutorProps.class)) {
                    List<TpExecutorProps> executors = (List<TpExecutorProps>) candidateExecutor;
                    if (CollectionUtils.isEmpty(executors)) {
                        return;
                    }
                    int[] idx = {0};
                    executors.forEach(executor -> {
                        tpExecutorPropFields.forEach(field -> setBasicField(source, field, candidateExecutorFieldName, executor, idx));
                        String executorFieldNamePrefix = MAIN_PROPERTIES_PREFIX + "." + candidateExecutorFieldName + "[" + idx[0] + "]";
                        setCollectionField(source, globalExecutorProps, executor, executorFieldNamePrefix);
                        idx[0]++;
                    });
                }
            }
        });
    }

    private static Object getProperty(String key, Object environment) {
        if (environment instanceof Map) {
            Map<?, Object> properties = (Map<?, Object>) environment;
            return properties.get(key);
        } else {
            return ContextManagerHelper.getEnvironmentProperty(key, environment);
        }
    }

    private static void setBasicField(Object source, Field field, String executorFieldName, Object executor, int[] idx) {
        String propKey = MAIN_PROPERTIES_PREFIX + "." + executorFieldName + "[" + idx[0] + "]." + field.getName();
        setBasicField(source, field, executor, propKey);
    }

    private static void setBasicField(Object source, Field field, String executorFieldName, Object executor) {
        String propKey = MAIN_PROPERTIES_PREFIX + "." + executorFieldName + "." + field.getName();
        setBasicField(source, field, executor, propKey);
    }

    private static void setBasicField(Object source, Field field, Object executor, String propKey) {
        Object propVal = getProperty(propKey, source);
        if (Objects.nonNull(propVal)) {
            return;
        }
        Object globalFieldVal = getProperty(GLOBAL_CONFIG_PREFIX + field.getName(), source);
        if (Objects.isNull(globalFieldVal)) {
            return;
        }
        ReflectUtil.setFieldValue(executor, field.getName(), globalFieldVal);
    }

    private static void setCollectionField(Object source, DtpExecutorProps globalExecutorProps, Object executor, String prefix) {
        if (isNotContains(prefix + ".taskWrapperNames[0]", source) &&
                CollectionUtils.isNotEmpty(globalExecutorProps.getTaskWrapperNames())) {
            ReflectUtil.setFieldValue(executor, "taskWrapperNames", globalExecutorProps.getTaskWrapperNames());
        }
        if (isNotContains(prefix + ".platformIds[0]", source) &&
                CollectionUtils.isNotEmpty(globalExecutorProps.getPlatformIds())) {
            ReflectUtil.setFieldValue(executor, PLATFORM_IDS, globalExecutorProps.getPlatformIds());
        }
        if (isNotContains(prefix + ".notifyItems[0].type", source) &&
                CollectionUtils.isNotEmpty(globalExecutorProps.getNotifyItems())) {
            ReflectUtil.setFieldValue(executor, NOTIFY_ITEMS, globalExecutorProps.getNotifyItems());
        }
        if (isNotContains(prefix + ".awareNames[0]", source) &&
                CollectionUtils.isNotEmpty(globalExecutorProps.getAwareNames())) {
            ReflectUtil.setFieldValue(executor, AWARE_NAMES, globalExecutorProps.getAwareNames());
        }
        try {
            if (isNotContains(prefix + ".pluginNames[0]", source) &&
                    CollectionUtils.isNotEmpty(globalExecutorProps.getPluginNames())) {
                ReflectUtil.setFieldValue(executor, PLUGIN_NAMES, globalExecutorProps.getPluginNames());
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private static boolean isNotContains(String key, Object environment) {
        return !contains(key, environment);
    }

    private static boolean contains(String key, Object environment) {
        if (environment instanceof Map) {
            Map<?, Object> properties = (Map<?, Object>) environment;
            return properties.containsKey(key);
        } else {
            return StringUtils.isNotBlank(ContextManagerHelper.getEnvironmentProperty(key, environment));
        }
    }
}
