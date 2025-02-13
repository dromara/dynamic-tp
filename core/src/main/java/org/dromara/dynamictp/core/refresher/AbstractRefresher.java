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

package org.dromara.dynamictp.core.refresher;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.em.ConfigFileTypeEnum;
import org.dromara.dynamictp.common.event.RefreshEvent;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.handler.ConfigHandler;
import org.dromara.dynamictp.core.support.binder.BinderHelper;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.dromara.dynamictp.common.manager.EventBusManager;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.MAIN_PROPERTIES_PREFIX;

/**
 * AbstractRefresher related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public abstract class AbstractRefresher implements Refresher {

    protected final DtpProperties dtpProperties;

    protected AbstractRefresher(DtpProperties dtpProperties) {
        this.dtpProperties = dtpProperties;
    }

    @Override
    public void refresh(String content, ConfigFileTypeEnum fileType) {
        if (StringUtils.isBlank(content) || Objects.isNull(fileType)) {
            log.warn("DynamicTp refresh, empty content or null fileType.");
            return;
        }

        try {
            val configHandler = ConfigHandler.getInstance();
            val properties = configHandler.parseConfig(content, fileType);
            refresh(properties);
        } catch (IOException e) {
            log.error("DynamicTp refresh error, content: {}, fileType: {}", content, fileType, e);
        }
    }

    protected void refresh(Map<Object, Object> properties) {
        if (MapUtils.isEmpty(properties)) {
            log.warn("DynamicTp refresh, empty properties.");
            return;
        }
        BinderHelper.bindDtpProperties(properties, dtpProperties);
        doRefresh(dtpProperties);
    }

    protected void refresh(Object environment) {
        BinderHelper.bindDtpProperties(environment, dtpProperties);
        doRefresh(dtpProperties);
    }

    protected void doRefresh(DtpProperties properties) {
        DtpRegistry.refresh(properties);
        publishEvent(properties);
    }

    protected boolean needRefresh(Set<String> changedKeys) {
        if (CollectionUtils.isEmpty(changedKeys)) {
            return false;
        }
        changedKeys = changedKeys.stream()
                .filter(str -> str.startsWith(MAIN_PROPERTIES_PREFIX))
                .collect(Collectors.toSet());
        return CollectionUtils.isNotEmpty(changedKeys);
    }

    private void publishEvent(DtpProperties dtpProperties) {
        RefreshEvent event = new RefreshEvent(this, dtpProperties);
        EventBusManager.post(event);
    }
}
