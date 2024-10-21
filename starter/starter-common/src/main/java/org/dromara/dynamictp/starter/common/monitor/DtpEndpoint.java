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

package org.dromara.dynamictp.starter.common.monitor;

import cn.hutool.core.io.FileUtil;
import com.google.common.collect.Lists;
import lombok.val;
import org.apache.commons.collections4.MapUtils;
import org.dromara.dynamictp.common.entity.JvmStats;
import org.dromara.dynamictp.common.entity.Metrics;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.converter.ExecutorConverter;
import org.dromara.dynamictp.common.manager.ContextManagerHelper;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.aware.MetricsAware;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.List;

/**
 * DtpEndpoint related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Endpoint(id = "dynamic-tp")
public class DtpEndpoint {

    @ReadOperation
    public List<Metrics> invoke() {

        List<Metrics> metricsList = Lists.newArrayList();
        DtpRegistry.getAllExecutorNames().forEach(x -> {
            ExecutorWrapper wrapper = DtpRegistry.getExecutorWrapper(x);
            metricsList.add(ExecutorConverter.toMetrics(wrapper));
        });

        val handlerMap = ContextManagerHelper.getBeansOfType(MetricsAware.class);
        if (MapUtils.isNotEmpty(handlerMap)) {
            handlerMap.forEach((k, v) -> metricsList.addAll(v.getMultiPoolStats()));
        }
        JvmStats jvmStats = new JvmStats();
        Runtime runtime = Runtime.getRuntime();
        jvmStats.setMaxMemory(FileUtil.readableFileSize(runtime.maxMemory()));
        jvmStats.setTotalMemory(FileUtil.readableFileSize(runtime.totalMemory()));
        jvmStats.setFreeMemory(FileUtil.readableFileSize(runtime.freeMemory()));
        jvmStats.setUsableMemory(FileUtil.readableFileSize(runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory()));
        metricsList.add(jvmStats);
        return metricsList;
    }
}
