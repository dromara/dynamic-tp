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

package org.dromara.dynamictp.starter.adapter.dubbo.autoconfigure.condition;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Condition;

/**
 * {@link Condition} that checks if the application is a dubbo application
 *
 * @author yanhom
 * @since 1.0.6
 */
public class OnApacheDubboCondition extends AnyNestedCondition {

    OnApacheDubboCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnBean(type = "org.apache.dubbo.config.spring.beans.factory.annotation.ServiceClassPostProcessor")
    static class ServiceClassBpp { }

    @ConditionalOnBean(type = "org.apache.dubbo.config.spring.beans.factory.annotation.ServiceAnnotationPostProcessor")
    static class ServiceAnnotationBpp { }

    @ConditionalOnBean(type = "org.apache.dubbo.config.spring.beans.factory.annotation.ServiceAnnotationBeanPostProcessor")
    static class ServiceAnnotationBeanBpp { }

}
