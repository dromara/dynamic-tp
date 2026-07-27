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

package org.dromara.dynamictp.starter.common.binder;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.DtpPropertiesBinderUtil;
import org.dromara.dynamictp.core.support.binder.PropertiesBinder;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;

import java.util.Map;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.MAIN_PROPERTIES_PREFIX;

/**
 * Spring Boot 2+/3+/4+ properties binder via {@link Binder}.
 *
 * <p>Boot 1.x {@code RelaxedDataBinder} path was removed on the Spring Boot 4 line.</p>
 *
 * @author yanhom
 * @since 1.0.3
 **/
@Slf4j
public class SpringBootPropertiesBinder implements PropertiesBinder {

    @Override
    public void bindDtpProperties(Map<?, Object> properties, DtpProperties dtpProperties) {
        beforeBind(properties, dtpProperties);
        ConfigurationPropertySource sources = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(sources);
        ResolvableType type = ResolvableType.forClass(DtpProperties.class);
        Bindable<?> target = Bindable.of(type).withExistingValue(dtpProperties);
        binder.bind(MAIN_PROPERTIES_PREFIX, target);
        afterBind(properties, dtpProperties);
    }

    @Override
    public void bindDtpProperties(Object environment, DtpProperties dtpProperties) {
        if (!(environment instanceof Environment)) {
            throw new IllegalArgumentException(
                    "Invalid environment type, expected org.springframework.core.env.Environment");
        }
        Environment env = (Environment) environment;
        beforeBind(env, dtpProperties);
        Binder binder = Binder.get(env);
        ResolvableType type = ResolvableType.forClass(DtpProperties.class);
        Bindable<?> target = Bindable.of(type).withExistingValue(dtpProperties);
        binder.bind(MAIN_PROPERTIES_PREFIX, target);
        afterBind(environment, dtpProperties);
    }

    @Override
    public void afterBind(Object source, DtpProperties dtpProperties) {
        DtpPropertiesBinderUtil.tryResetWithGlobalConfig(source, dtpProperties);
    }
}
