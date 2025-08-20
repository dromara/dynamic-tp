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

package org.dromara.dynamictp.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.executor.OrderedDtpExecutor;
import org.dromara.dynamictp.example.service.TestService;
import org.dromara.dynamictp.client.adminclient.AdminClient;
import org.dromara.dynamictp.common.em.AdminRequestTypeEnum;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * TestServiceImpl related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Slf4j
@Service
public class TestServiceImpl implements TestService {

    private final ThreadPoolExecutor jucThreadPoolExecutor;

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private final DtpExecutor eagerDtpExecutor;

    private final ScheduledExecutorService scheduledDtpExecutor;

    private final OrderedDtpExecutor orderedDtpExecutor;

    private AdminClient adminClient;

    public TestServiceImpl(ThreadPoolExecutor jucThreadPoolExecutor,
            ThreadPoolTaskExecutor threadPoolTaskExecutor,
            DtpExecutor eagerDtpExecutor,
            ScheduledExecutorService scheduledDtpExecutor,
            OrderedDtpExecutor orderedDtpExecutor,
            AdminClient adminClient) {
        this.jucThreadPoolExecutor = jucThreadPoolExecutor;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.eagerDtpExecutor = eagerDtpExecutor;
        this.scheduledDtpExecutor = scheduledDtpExecutor;
        this.orderedDtpExecutor = orderedDtpExecutor;
        this.adminClient = adminClient;
    }

    @Override
    public Object testAdminClient() {
        Object resp = adminClient.requestToServer(AdminRequestTypeEnum.ALARM_MANAGE);
        log.info("testAdminClient,remoteAddress:{}", adminClient.getConnection().getRemoteAddress());
        return resp;
    }

    @Override
    public Object testAdminClient(AdminRequestTypeEnum type) {
        Object resp = adminClient.requestToServer(type);
        log.info("testAdminClient type:{}, remoteAddress:{}", type, adminClient.getConnection().getRemoteAddress());
        return resp;
    }

    @Override
    public java.util.Map<String, Object> testAdminClientAll() {
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        for (AdminRequestTypeEnum type : AdminRequestTypeEnum.values()) {
            Object resp = adminClient.requestToServer(type);
            log.info("testAdminClient type:{}, remoteAddress:{}", type, adminClient.getConnection().getRemoteAddress());
            result.put(type.name(), resp);
        }
        return result;
    }

}
