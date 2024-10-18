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

package org.dromara.dynamictp.core.handler;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.common.util.ExtensionServiceLoader;
import org.dromara.dynamictp.core.monitor.collector.InternalLogCollector;
import org.dromara.dynamictp.core.monitor.collector.LogCollector;
import org.dromara.dynamictp.core.monitor.collector.MetricsCollector;
import org.dromara.dynamictp.core.monitor.collector.MicroMeterCollector;
import org.dromara.dynamictp.core.monitor.collector.jmx.JMXCollector;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * CollectorHandler related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public final class CollectorHandler {

    private static final Map<String, MetricsCollector> COLLECTORS = Maps.newHashMap();

    private CollectorHandler() {
        List<MetricsCollector> loadedCollectors = ExtensionServiceLoader.get(MetricsCollector.class);
        loadedCollectors.forEach(collector -> COLLECTORS.put(collector.type().toLowerCase(), collector));

        MetricsCollector microMeterCollector = new MicroMeterCollector();
        LogCollector logCollector = new LogCollector();
        InternalLogCollector internalLogCollector = new InternalLogCollector();
        JMXCollector jmxCollector = new JMXCollector();
        COLLECTORS.put(microMeterCollector.type(), microMeterCollector);
        COLLECTORS.put(logCollector.type(), logCollector);
        COLLECTORS.put(internalLogCollector.type(), internalLogCollector);
        COLLECTORS.put(jmxCollector.type(), jmxCollector);
    }

    public void collect(ThreadPoolStats poolStats, List<String> types) {
        if (poolStats == null || CollectionUtils.isEmpty(types)) {
            return;
        }
        for (String collectorType : types) {
            MetricsCollector collector = COLLECTORS.get(collectorType.toLowerCase());
            if (collector != null) {
                collector.collect(poolStats);
            }
        }
    }

    public static CollectorHandler getInstance() {
        return CollectorHandlerHolder.INSTANCE;
    }

    private static class CollectorHandlerHolder {
        private static final CollectorHandler INSTANCE = new CollectorHandler();
    }
}
