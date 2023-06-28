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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.plugin.ExtensionRegistry;
import org.dromara.dynamictp.core.plugin.ConstructorUtil;
import org.dromara.dynamictp.core.support.DynamicTp;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.TaskQueue;
import org.dromara.dynamictp.core.thread.DtpExecutor;
import org.dromara.dynamictp.core.thread.EagerDtpExecutor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.type.MethodMetadata;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * BeanPostProcessor that handles all related beans managed by Spring.
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpPostProcessor implements BeanPostProcessor, BeanFactoryAware, PriorityOrdered {

    private DefaultListableBeanFactory beanFactory;

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (!(bean instanceof ThreadPoolExecutor) && !(bean instanceof ThreadPoolTaskExecutor)) {
            return bean;
        }

        if (bean instanceof DtpExecutor) {
            // register DtpExecutor
            DtpExecutor dtpExecutor = (DtpExecutor) bean;
            Object[] args = ConstructorUtil.buildDtpExecutorConstructorArgs(dtpExecutor);
            Class[] argTypes = ConstructorUtil.buildDtpExecutorConstructorArgTypes();
            bean = ExtensionRegistry.pluginAll(bean, argTypes, args);
            registerDtp(bean);
        } else {
            // register ThreadPoolExecutor or ThreadPoolTaskExecutor
            registerCommon(bean, beanName);
        }
        return bean;
    }

    private void registerDtp(Object bean) {
        DtpExecutor dtpExecutor = (DtpExecutor) bean;
        if (bean instanceof EagerDtpExecutor) {
            ((TaskQueue) dtpExecutor.getQueue()).setExecutor((EagerDtpExecutor) dtpExecutor);
        }
        DtpRegistry.registerExecutor(ExecutorWrapper.of(dtpExecutor), "beanPostProcessor");
    }

    private void registerCommon(Object bean, String beanName) {
        String dtpAnnotationVal;
        try {
            DynamicTp dynamicTp = beanFactory.findAnnotationOnBean(beanName, DynamicTp.class);
            if (Objects.nonNull(dynamicTp)) {
                dtpAnnotationVal = dynamicTp.value();
            } else {
                BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
                if (!(beanDefinition instanceof AnnotatedBeanDefinition)) {
                    return;
                }
                AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
                MethodMetadata methodMetadata = (MethodMetadata) annotatedBeanDefinition.getSource();
                if (Objects.isNull(methodMetadata) || !methodMetadata.isAnnotated(DynamicTp.class.getName())) {
                    return;
                }
                dtpAnnotationVal = Optional.ofNullable(methodMetadata.getAnnotationAttributes(DynamicTp.class.getName()))
                        .orElse(Collections.emptyMap())
                        .getOrDefault("value", "")
                        .toString();
            }
        } catch (NoSuchBeanDefinitionException e) {
            log.error("There is no bean with the given name {}", beanName, e);
            return;
        }
        String poolName = StringUtils.isNotBlank(dtpAnnotationVal) ? dtpAnnotationVal : beanName;
        Executor executor;
        if (bean instanceof ThreadPoolTaskExecutor) {
            executor = ((ThreadPoolTaskExecutor) bean).getThreadPoolExecutor();
        } else {
            executor = (Executor) bean;
        }
        DtpRegistry.registerExecutor(new ExecutorWrapper(poolName, executor), "beanPostProcessor");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
