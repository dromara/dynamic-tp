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

package org.dromara.dynamictp.adapter.liteflow;

import com.google.common.collect.Maps;
import com.yomahub.liteflow.thread.ExecutorHelper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.MapUtils;
import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.proxy.ThreadPoolExecutorProxy;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * LiteflowDtpAdapter related
 *
 * @author yanhom
 * @since 1.1.9
 */
@Slf4j
@SuppressWarnings("all")
public class LiteflowDtpAdapter extends AbstractDtpAdapter {

    private static final String TP_PREFIX = "liteflowTp";

    private static final String EXECUTOR_MAP_FIELD = "executorServiceMap";

    @Override
    protected String getTpPrefix() {
        return TP_PREFIX;
    }

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(dtpProperties.getLiteflowTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected void initialize() {
        super.initialize();
        val executorHelper = ExecutorHelper.loadInstance();
        val executorMap = (Map<String, ExecutorService>) ReflectionUtil.getFieldValue(
                ExecutorHelper.class, EXECUTOR_MAP_FIELD, executorHelper);
        if (MapUtils.isEmpty(executorMap)) {
            log.warn("Cannot find instances of type ExecutorService.");
            return;
        }
        Map<String, ExecutorService> newExecutorMap = Maps.newHashMap();
        executorMap.forEach((k, v) -> {
            String key = k.substring(k.lastIndexOf(".") + 1);
            val tpName = TP_PREFIX + "#" + key;
            ThreadPoolExecutor executor;
            if (v instanceof ThreadPoolExecutor) {
                executor = (ThreadPoolExecutor) v;
            } else {
                executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue("executorService", v);
            }
            ThreadPoolExecutorProxy proxy = new ThreadPoolExecutorProxy(executor);
            newExecutorMap.put(k, proxy);
            executors.put(tpName, new ExecutorWrapper(tpName, proxy));
        });
        ReflectionUtil.setFieldValue(EXECUTOR_MAP_FIELD, executorHelper, newExecutorMap);
        executorMap.forEach((k, v) -> shutdownOriginalExecutor(v));
    }
}
