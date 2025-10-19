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

package org.dromara.dynamictp.example.collector;

import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.common.util.JsonUtil;
import org.dromara.dynamictp.core.monitor.collector.AbstractCollector;

/**
 * EsCollector related
 *
 * @author yanhom
 * @since 1.1.0
 */
public class EsCollector extends AbstractCollector {

    private final EsClient esClient;

    public EsCollector() {
        this.esClient = new EsClient();
    }

    @Override
    public void collect(ThreadPoolStats poolStats) {
        esClient.save(JsonUtil.toJson(poolStats));
    }

    @Override
    public String type() {
        return "es";
    }
}
