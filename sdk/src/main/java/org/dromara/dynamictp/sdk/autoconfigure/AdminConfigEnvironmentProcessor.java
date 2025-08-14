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

package org.dromara.dynamictp.sdk.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.em.AdminRequestTypeEnum;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.support.binder.BinderHelper;
import org.dromara.dynamictp.sdk.client.AdminClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Map;

/**
 * ZkConfigEnvironmentProcessor related
 *
 * @author yanhom
 * @since 1.0.4
 **/
@Slf4j
public class AdminConfigEnvironmentProcessor implements EnvironmentPostProcessor, Ordered {

//    AdminClient adminClient;

//    public AdminConfigEnvironmentProcessor(AdminClient adminClient) {
//        this.adminClient = adminClient;
//    }

    public static final String ADMIN_PROPERTY_SOURCE_NAME = "dtpAdminPropertySource";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
//        DtpProperties dtpProperties = DtpProperties.getInstance();
//        BinderHelper.bindDtpProperties(environment, dtpProperties);
//        Map<Object, Object> properties = (Map<Object, Object>) adminClient.requestToServer(AdminRequestTypeEnum.EXECUTOR_REFRESH);
//        if (!checkPropertyExist(environment)) {
//            createAdminPropertySource(environment, properties);
//        }
    }

    private boolean checkPropertyExist(ConfigurableEnvironment environment) {
        MutablePropertySources propertySources = environment.getPropertySources();
        return propertySources.stream().anyMatch(p -> ADMIN_PROPERTY_SOURCE_NAME.equals(p.getName()));
    }

    private void createAdminPropertySource(ConfigurableEnvironment environment, Map<Object, Object> properties) {
        MutablePropertySources propertySources = environment.getPropertySources();
        OriginTrackedMapPropertySource adminSource = new OriginTrackedMapPropertySource(ADMIN_PROPERTY_SOURCE_NAME, properties);
        propertySources.addLast(adminSource);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
