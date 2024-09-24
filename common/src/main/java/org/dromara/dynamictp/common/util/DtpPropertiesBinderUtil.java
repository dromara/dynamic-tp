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
import org.dromara.dynamictp.common.entity.DtpExecutorProps;
import org.dromara.dynamictp.common.entity.TpExecutorProps;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.springframework.core.env.Environment;

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
 * @since 1.1.0
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
        int[] idx = {0};
        dtpProperties.getExecutors().forEach(executor -> {
            dtpPropsFields.forEach(field -> {
                String executorFieldKey = EXECUTORS_CONFIG_PREFIX + idx[0] + "]." + field.getName();
                setBasicField(source, field, executor, executorFieldKey);
            });
            String executorPropKeyPrefix = EXECUTORS_CONFIG_PREFIX + idx[0] + "]";
            setListField(dtpProperties, executor, executorPropKeyPrefix, source);
            val globalExecutorProps = dtpProperties.getGlobalExecutorProps();
            if (!contains(executorPropKeyPrefix + ".pluginNames[0]", source) &&
                    CollectionUtils.isNotEmpty(globalExecutorProps.getPluginNames())) {
                ReflectUtil.setFieldValue(executor, PLUGIN_NAMES, globalExecutorProps.getPluginNames());
            }
            idx[0]++;
        });
    }

    private static void tryResetAdapterExecutors(DtpProperties dtpProperties, Object source) {
        val dtpPropertiesFields = ReflectionUtil.getAllFields(DtpProperties.class);
        val tpExecutorPropFields = ReflectionUtil.getAllFields(TpExecutorProps.class);
        dtpPropertiesFields.forEach(dtpPropertiesField -> {
            val targetObj = ReflectUtil.getFieldValue(dtpProperties, dtpPropertiesField);
            if (Objects.isNull(targetObj)) {
                return;
            }
            if (dtpPropertiesField.getType().isAssignableFrom(TpExecutorProps.class)) {
                tpExecutorPropFields.forEach(tpField -> setBasicField(source, tpField, dtpPropertiesField.getName(), targetObj));
                String prefix = MAIN_PROPERTIES_PREFIX + "." + dtpPropertiesField.getName();
                setListField(dtpProperties, targetObj, prefix, source);
            } else if (dtpPropertiesField.getGenericType() instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) dtpPropertiesField.getGenericType();
                Type[] argTypes = paramType.getActualTypeArguments();
                if (argTypes.length == 1 && argTypes[0].equals(TpExecutorProps.class)) {
                    List<TpExecutorProps> tpExecutorProps = (List<TpExecutorProps>) targetObj;
                    if (CollectionUtils.isEmpty(tpExecutorProps)) {
                        return;
                    }
                    int[] idx = {0};
                    tpExecutorProps.forEach(tpProp -> {
                        tpExecutorPropFields.forEach(tpField -> setBasicField(source, tpField, dtpPropertiesField.getName(), tpProp, idx));
                        String prefix = MAIN_PROPERTIES_PREFIX + "." + dtpPropertiesField.getName() + "[" + idx[0] + "]";
                        setListField(dtpProperties, tpProp, prefix, source);
                        idx[0]++;
                    });
                }
            }
        });
    }

    private static Object getProperty(String key, Object environment) {
        if (environment instanceof Environment) {
            Environment env = (Environment) environment;
            return env.getProperty(key);
        } else if (environment instanceof Map) {
            Map<?, Object> properties = (Map<?, Object>) environment;
            return properties.get(key);
        }
        return null;
    }

    private static boolean contains(String key, Object environment) {
        if (environment instanceof Environment) {
            Environment env = (Environment) environment;
            return env.containsProperty(key);
        } else if (environment instanceof Map) {
            Map<?, Object> properties = (Map<?, Object>) environment;
            return properties.containsKey(key);
        }
        return false;
    }

    private static void setBasicField(Object source, Field tpPropField, String targetObjName, Object targetObj, int[] idx) {
        String executorFieldKey = MAIN_PROPERTIES_PREFIX + "." + targetObjName + "[" + idx[0] + "]." + tpPropField.getName();
        setBasicField(source, tpPropField, targetObj, executorFieldKey);
    }

    private static void setBasicField(Object source, Field tpPropField, String targetObjName, Object targetObj) {
        String executorFieldKey = MAIN_PROPERTIES_PREFIX + "." + targetObjName + "." + tpPropField.getName();
        setBasicField(source, tpPropField, targetObj, executorFieldKey);
    }

    private static void setBasicField(Object source, Field tpPropField, Object targetObj, String executorFieldKey) {
        Object executorFieldVal = getProperty(executorFieldKey, source);
        if (Objects.nonNull(executorFieldVal)) {
            return;
        }
        Object globalFieldVal = getProperty(GLOBAL_CONFIG_PREFIX + tpPropField.getName(), source);
        if (Objects.isNull(globalFieldVal)) {
            return;
        }
        ReflectUtil.setFieldValue(targetObj, tpPropField.getName(), globalFieldVal);
    }

    private static void setListField(DtpProperties dtpProperties, Object fieldVal, String prefix, Object source) {
        val globalExecutorProps = dtpProperties.getGlobalExecutorProps();
        if (!contains(prefix + ".taskWrapperNames[0]", source) &&
                CollectionUtils.isNotEmpty(globalExecutorProps.getTaskWrapperNames())) {
            ReflectUtil.setFieldValue(fieldVal, "taskWrapperNames", globalExecutorProps.getTaskWrapperNames());
        }
        if (!contains(prefix + ".platformIds[0]", source) &&
                CollectionUtils.isNotEmpty(globalExecutorProps.getPlatformIds())) {
            ReflectUtil.setFieldValue(fieldVal, PLATFORM_IDS, globalExecutorProps.getPlatformIds());
        }
        if (!contains(prefix + ".notifyItems[0].type", source) &&
                CollectionUtils.isNotEmpty(globalExecutorProps.getNotifyItems())) {
            ReflectUtil.setFieldValue(fieldVal, NOTIFY_ITEMS, globalExecutorProps.getNotifyItems());
        }
        if (!contains(prefix + ".awareNames[0]", source) &&
                CollectionUtils.isNotEmpty(globalExecutorProps.getAwareNames())) {
            ReflectUtil.setFieldValue(fieldVal, AWARE_NAMES, globalExecutorProps.getAwareNames());
        }
    }
}
