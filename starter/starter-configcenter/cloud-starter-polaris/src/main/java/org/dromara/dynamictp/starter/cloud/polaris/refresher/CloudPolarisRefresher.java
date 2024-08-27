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

package org.dromara.dynamictp.starter.cloud.polaris.refresher;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.spring.AbstractSpringRefresher;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.lang.NonNull;

/**
 * CloudPolarisRefresher related
 *
 * @author fabian4
 * @since 1.0.0
 **/
@Slf4j
public class CloudPolarisRefresher extends AbstractSpringRefresher implements SmartApplicationListener {

    public CloudPolarisRefresher(DtpProperties dtpProperties) {
        super(dtpProperties);
    }

    @Override
    public boolean supportsEventType(@NonNull Class<? extends ApplicationEvent> eventType) {
        return EnvironmentChangeEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationEvent event) {
        if (needRefresh(((EnvironmentChangeEvent) event).getKeys())) {
            refresh(environment);
        }
    }
}
