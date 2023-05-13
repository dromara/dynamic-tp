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

package org.dromara.dynamictp.adapter.tars;

import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import com.qq.tars.client.Communicator;
import com.qq.tars.client.CommunicatorFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.MapUtils;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * TarsDtpAdapter related
 *
 * @author yanhom
 * @since 1.1.0
 */
@SuppressWarnings("unchecked")
@Slf4j
public class TarsDtpAdapter extends AbstractDtpAdapter {

    private static final String NAME = "tarsTp";

    private static final String COMMUNICATORS_FIELD = "CommunicatorMap";

    private static final String THREAD_POOL_FIELD = "threadPoolExecutor";

    private static final String COMMUNICATOR_ID_FIELD = "id";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getTarsTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected void initialize() {
        super.initialize();

        CommunicatorFactory communicatorFactory = CommunicatorFactory.getInstance();
        val communicatorMap = (ConcurrentHashMap<Object, Communicator>) ReflectionUtil.getFieldValue(
                CommunicatorFactory.class, COMMUNICATORS_FIELD, communicatorFactory);
        if (MapUtils.isEmpty(communicatorMap)) {
            log.warn("Cannot find instances of type Communicator.");
            return;
        }
        communicatorMap.forEach((k, v) -> {
            val executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(Communicator.class, THREAD_POOL_FIELD, v);
            if (Objects.isNull(executor)) {
                return;
            }
            val id = (String) ReflectionUtil.getFieldValue(Communicator.class, COMMUNICATOR_ID_FIELD, v);
            val executorWrapper = new ExecutorWrapper(id, executor);
            executorWrapper.setThreadPoolAliasName(v.getCommunicatorConfig().getLocator());
            initNotifyItems(id, executorWrapper);
            executors.put(id, executorWrapper);
        });
        log.info("DynamicTp adapter, tars executors init end, executors: {}", executors);
    }
}
