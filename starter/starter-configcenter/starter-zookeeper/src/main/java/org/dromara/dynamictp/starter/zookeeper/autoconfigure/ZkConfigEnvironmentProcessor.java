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

package org.dromara.dynamictp.starter.zookeeper.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.support.binder.BinderHelper;
import org.dromara.dynamictp.starter.zookeeper.util.CuratorUtil;
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
public class ZkConfigEnvironmentProcessor implements EnvironmentPostProcessor, Ordered {

    public static final String ZK_PROPERTY_SOURCE_NAME = "dtpZkPropertySource";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        DtpProperties dtpProperties = DtpProperties.getInstance();
        BinderHelper.bindDtpProperties(environment, dtpProperties);
        Map<Object, Object> properties = CuratorUtil.genPropertiesMap(dtpProperties);
        if (!checkPropertyExist(environment)) {
            createZkPropertySource(environment, properties);
        }
    }

    private boolean checkPropertyExist(ConfigurableEnvironment environment) {
        MutablePropertySources propertySources = environment.getPropertySources();
        return propertySources.stream().anyMatch(p -> ZK_PROPERTY_SOURCE_NAME.equals(p.getName()));
    }

    private void createZkPropertySource(ConfigurableEnvironment environment, Map<Object, Object> properties) {
        MutablePropertySources propertySources = environment.getPropertySources();
        OriginTrackedMapPropertySource zkSource = new OriginTrackedMapPropertySource(ZK_PROPERTY_SOURCE_NAME, properties);
        propertySources.addLast(zkSource);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
