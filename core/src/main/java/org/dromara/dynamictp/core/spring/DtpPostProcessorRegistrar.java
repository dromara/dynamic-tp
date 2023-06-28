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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * DtpPostProcessorRegistrar related
 *
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
public class DtpPostProcessorRegistrar implements ImportBeanDefinitionRegistrar {

    private static final String BEAN_NAME = "dtpPostProcessor";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(BEAN_NAME)) {
            AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder
                    .genericBeanDefinition(DtpPostProcessor.class,
                            DtpPostProcessor::new)
                    .getBeanDefinition();
            beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            beanDefinition.setSynthetic(true);
            registry.registerBeanDefinition(BEAN_NAME, beanDefinition);
        }
    }

}
