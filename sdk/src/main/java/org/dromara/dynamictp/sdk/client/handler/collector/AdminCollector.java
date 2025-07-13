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

package org.dromara.dynamictp.sdk.client.handler.collector;

import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.core.monitor.collector.AbstractCollector;
import org.dromara.dynamictp.sdk.client.AdminClient;
import org.dromara.dynamictp.sdk.client.AdminRequestBody;
import org.dromara.dynamictp.sdk.client.AdminRequestTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;

public class AdminCollector extends AbstractCollector {

    @Autowired
    private AdminClient adminClient;

    @Override
    public void collect(ThreadPoolStats poolStats) {
        AdminRequestBody adminRequestBody = new AdminRequestBody(AdminRequestTypeEnum.EXECUTOR_MONITOR, poolStats);
        adminClient.invokeSync(adminRequestBody);
    }

    @Override
    public String type() {
        return "admin";
    }
}
