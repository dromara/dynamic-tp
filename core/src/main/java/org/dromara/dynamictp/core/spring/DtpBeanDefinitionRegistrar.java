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

package org.dromara.dynamictp.core.spring;

import org.dromara.dynamictp.common.entity.DtpExecutorProps;
import org.dromara.dynamictp.common.entity.DtpExtensionProps;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.BeanUtil;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.common.util.StringUtil;
import org.dromara.dynamictp.core.plugin.ExtensionRegistry;
import org.dromara.dynamictp.core.plugin.DtpExtension;
import org.dromara.dynamictp.core.reject.RejectHandlerGetter;
import org.dromara.dynamictp.core.support.ExecutorType;
import org.dromara.dynamictp.core.support.TaskQueue;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrappers;
import org.dromara.dynamictp.core.thread.EagerDtpExecutor;
import org.dromara.dynamictp.core.thread.NamedThreadFactory;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.ALLOW_CORE_THREAD_TIMEOUT;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.AWAIT_TERMINATION_SECONDS;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.FAIR;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.MAXFREEMEMORY;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.NOTIFY_ENABLED;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.NOTIFY_ITEMS;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.PLATFORM_IDS;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.PRE_START_ALL_CORE_THREADS;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.QUEUE_TIMEOUT;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.REJECT_ENHANCED;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.REJECT_HANDLER_TYPE;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.RUN_TIMEOUT;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.TASK_WRAPPERS;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.THREAD_POOL_ALIAS_NAME;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.THREAD_POOL_NAME;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.WAIT_FOR_TASKS_TO_COMPLETE_ON_SHUTDOWN;
import static org.dromara.dynamictp.common.em.QueueTypeEnum.buildLbq;
import static org.dromara.dynamictp.common.entity.NotifyItem.mergeAllNotifyItems;

/**
 * DtpBeanDefinitionRegistrar related
 *
 * @author yanhom
 * @since 1.0.4
 **/
@Slf4j
public class DtpBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        DtpProperties dtpProperties = new DtpProperties();
        PropertiesBinder.bindDtpProperties(environment, dtpProperties);
        val executors = dtpProperties.getExecutors();
        List<DtpExtensionProps> extensions = dtpProperties.getExtensions();
        registerDtpExtensions(extensions);
        if (CollectionUtils.isEmpty(executors)) {
            log.warn("DynamicTp registrar, no executors are configured.");
            return;
        }

        executors.forEach(e -> {
            Class<?> executorTypeClass = ExecutorType.getClass(e.getExecutorType());
            Map<String, Object> propertyValues = buildPropertyValues(e);
            Object[] args = buildConstructorArgs(executorTypeClass, e);
            BeanUtil.registerIfAbsent(registry, e.getThreadPoolName(), executorTypeClass, propertyValues, args);
        });
    }

    private void registerDtpExtensions(List<DtpExtensionProps> extensions) {

        for (DtpExtensionProps extension : extensions) {
            if (StringUtil.isEmpty(extension.getExtensionPath())) {
                log.warn("DynamicTp registrar, no extension is configured.");
                continue;
            }
            DtpExtension dtpExtension = (DtpExtension) ReflectionUtil.resolveObject(extension.getExtensionPath());
            ExtensionRegistry.registerExtension(dtpExtension);
        }
    }

    private Map<String, Object> buildPropertyValues(DtpExecutorProps props) {
        Map<String, Object> propertyValues = Maps.newHashMap();
        propertyValues.put(THREAD_POOL_NAME, props.getThreadPoolName());
        propertyValues.put(THREAD_POOL_ALIAS_NAME, props.getThreadPoolAliasName());
        propertyValues.put(ALLOW_CORE_THREAD_TIMEOUT, props.isAllowCoreThreadTimeOut());
        propertyValues.put(WAIT_FOR_TASKS_TO_COMPLETE_ON_SHUTDOWN, props.isWaitForTasksToCompleteOnShutdown());
        propertyValues.put(AWAIT_TERMINATION_SECONDS, props.getAwaitTerminationSeconds());
        propertyValues.put(PRE_START_ALL_CORE_THREADS, props.isPreStartAllCoreThreads());
        propertyValues.put(REJECT_HANDLER_TYPE, props.getRejectedHandlerType());
        propertyValues.put(REJECT_ENHANCED, props.isRejectEnhanced());
        propertyValues.put(RUN_TIMEOUT, props.getRunTimeout());
        propertyValues.put(QUEUE_TIMEOUT, props.getQueueTimeout());
        val notifyItems = mergeAllNotifyItems(props.getNotifyItems());
        propertyValues.put(NOTIFY_ITEMS, notifyItems);
        propertyValues.put(PLATFORM_IDS, props.getPlatformIds());
        propertyValues.put(NOTIFY_ENABLED, props.isNotifyEnabled());

        val taskWrappers = TaskWrappers.getInstance().getByNames(props.getTaskWrapperNames());
        propertyValues.put(TASK_WRAPPERS, taskWrappers);

        return propertyValues;
    }

    private Object[] buildConstructorArgs(Class<?> clazz, DtpExecutorProps props) {
        BlockingQueue<Runnable> taskQueue;
        if (clazz.equals(EagerDtpExecutor.class)) {
            taskQueue = new TaskQueue(props.getQueueCapacity());
        } else {
            taskQueue = buildLbq(props.getQueueType(),
                    props.getQueueCapacity(),
                    props.isFair(),
                    props.getMaxFreeMemory());
        }

        return new Object[] {
                props.getCorePoolSize(),
                props.getMaximumPoolSize(),
                props.getKeepAliveTime(),
                props.getUnit(),
                taskQueue,
                new NamedThreadFactory(props.getThreadNamePrefix()),
                RejectHandlerGetter.buildRejectedHandler(props.getRejectedHandlerType())
        };
    }
}
