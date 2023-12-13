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

package org.dromara.dynamictp.starter.apollo.refresher;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.refresher.AbstractRefresher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

/**
 * ApolloRefresher related
 * <p>Listen for configuration file changes</p>
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class ApolloRefresher extends AbstractRefresher implements ConfigChangeListener, InitializingBean {

    private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector
            .getInstance(ConfigPropertySourceFactory.class);

    @Override
    public void onChange(ConfigChangeEvent changeEvent) {
        refresh(environment);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<ConfigPropertySource> configPropertySources = configPropertySourceFactory.getAllConfigPropertySources();
        for (ConfigPropertySource configPropertySource : configPropertySources) {
            if (Arrays.stream(configPropertySource.getPropertyNames())
                    .anyMatch(propertyName -> propertyName.startsWith("spring.dynamic.tp"))) {
                configPropertySource.addChangeListener(this);
            }
        }
    }
}
