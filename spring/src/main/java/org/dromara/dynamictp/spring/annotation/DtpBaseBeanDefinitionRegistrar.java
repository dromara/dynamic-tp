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

package org.dromara.dynamictp.spring.annotation;

import com.google.common.collect.Lists;

import org.dromara.dynamictp.common.timer.HashedWheelTimer;
import org.dromara.dynamictp.core.executor.NamedThreadFactory;
import org.dromara.dynamictp.spring.DtpPostProcessor;
import org.dromara.dynamictp.spring.holder.SpringContextHolder;
import org.dromara.dynamictp.spring.util.BeanRegistrationUtil;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.concurrent.TimeUnit;

/**
 * DtpBaseBeanDefinitionRegistrar related
 *
 * @author yanhom
 * @since 1.0.4
 **/
public class DtpBaseBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    private static final String APPLICATION_CONTEXT_HOLDER = "dtpApplicationContextHolder";

    private static final String HASHED_WHEEL_TIMER = "dtpHashedWheelTimer";

    private static final String DTP_POST_PROCESSOR = "dtpPostProcessor";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        registerHashedWheelTimer(registry);
        BeanRegistrationUtil.registerIfAbsent(registry, APPLICATION_CONTEXT_HOLDER, SpringContextHolder.class);

        // ApplicationContextHolder and HashedWheelTimer are required in DtpExecutor execute method, so they must be registered first
        BeanRegistrationUtil.registerIfAbsent(registry, DTP_POST_PROCESSOR, DtpPostProcessor.class,
                null, Lists.newArrayList(APPLICATION_CONTEXT_HOLDER, HASHED_WHEEL_TIMER));
    }

    private void registerHashedWheelTimer(BeanDefinitionRegistry registry) {
        Object[] constructorArgs = new Object[] {
                new NamedThreadFactory("dtp-runnable-timeout", true),
                10,
                TimeUnit.MILLISECONDS
        };
        BeanRegistrationUtil.registerIfAbsent(registry, HASHED_WHEEL_TIMER, HashedWheelTimer.class, constructorArgs);
    }
}
