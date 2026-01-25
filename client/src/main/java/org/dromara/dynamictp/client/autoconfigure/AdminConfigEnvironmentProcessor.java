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

package org.dromara.dynamictp.client.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.client.AdminClient;
import org.dromara.dynamictp.client.AdminClientConstants;
import org.dromara.dynamictp.client.processor.ClientUserProcessor;
import org.dromara.dynamictp.client.properties.AdminClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Map;
import java.util.Objects;

/**
 * Admin config environment processor
 *
 * @author yanhom
 * @since 1.0.4
 **/
@Slf4j
public class AdminConfigEnvironmentProcessor implements EnvironmentPostProcessor, Ordered {

    public AdminConfigEnvironmentProcessor() {
    }

    public static final String ADMIN_PROPERTY_SOURCE_NAME = "dtpAdminPropertySource";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        AdminClientProperties props = bindAdminClientProperties(environment);

        // Fall back to spring.application.name when not configured
        String appName = environment.getProperty("spring.application.name");
        if (StringUtils.isBlank(props.getClientName())) {
            props.setClientName(Objects.requireNonNull(appName));
        }
        if (StringUtils.isBlank(props.getServiceName())) {
            props.setServiceName(Objects.requireNonNull(appName));
        }

        // Create AdminClient with configured properties
        AdminClient adminClient = new AdminClient(new ClientUserProcessor(), props);
        adminClient.init();
        Object response = adminClient.requestToServer(AdminClientConstants.REQUEST_TYPE_EXECUTOR_REFRESH);
        if (!checkPropertyExist(environment) && response instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> properties = (Map<Object, Object>) response;
            createAdminPropertySource(environment, properties);
        }
        adminClient.close();
    }

    private AdminClientProperties bindAdminClientProperties(ConfigurableEnvironment environment) {
        Binder binder = Binder.get(environment);

        AdminClientProperties props = binder.bind("dynamictp", Bindable.of(AdminClientProperties.class))
                .orElseGet(AdminClientProperties::new);

        // handle relaxed binding variations (e.g. dynamictp.load-balance-strategy)
        if (props.getLoadBalanceStrategy() == null || props.getLoadBalanceStrategy().trim().isEmpty()) {
            props.setLoadBalanceStrategy(environment.getProperty("dynamictp.loadBalanceStrategy", "roundRobin"));
        }

        return props;
    }

    private boolean checkPropertyExist(ConfigurableEnvironment environment) {
        MutablePropertySources propertySources = environment.getPropertySources();
        return propertySources.stream().anyMatch(p -> ADMIN_PROPERTY_SOURCE_NAME.equals(p.getName()));
    }

    private void createAdminPropertySource(ConfigurableEnvironment environment, Map<Object, Object> properties) {
        MutablePropertySources propertySources = environment.getPropertySources();
        OriginTrackedMapPropertySource adminSource = new OriginTrackedMapPropertySource(ADMIN_PROPERTY_SOURCE_NAME,
                properties);
        propertySources.addLast(adminSource);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
