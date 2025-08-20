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

package org.dromara.dynamictp.client.adminclient.processor;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.entity.AdminRequestBody;
import org.dromara.dynamictp.client.adminclient.handler.collector.AdminCollector;
import org.dromara.dynamictp.client.adminclient.handler.refresh.AdminRefresher;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AdminClientUserProcessor related
 *
 * @author eachann
 */
@Slf4j
public class AdminClientUserProcessor extends SyncUserProcessor<AdminRequestBody> {

    private final ExecutorService executor;

    @Getter
    private String remoteAddress = "NOT-CONNECT";

    private AdminRefresher adminRefresher;

    public AdminClientUserProcessor() {
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Autowired
    public AdminClientUserProcessor(AdminRefresher adminRefresher) {
        this.executor = Executors.newSingleThreadExecutor();
        this.adminRefresher = adminRefresher;
    }

    @Override
    public Object handleRequest(BizContext bizContext, AdminRequestBody adminRequestBody) throws Exception {
        log.info("DynamicTp admin request received:{}", adminRequestBody.getRequestType().getValue());
        if (bizContext.isRequestTimeout()) {
            log.warn("DynamicTp admin request timeout:{}s", bizContext.getClientTimeout());
        }
        this.remoteAddress = bizContext.getRemoteAddress();
        return doHandleRequest(adminRequestBody);
    }

    private Object doHandleRequest(AdminRequestBody adminRequestBody) {
        switch (adminRequestBody.getRequestType()) {
            case EXECUTOR_MONITOR:
                return handleExecutorMonitorRequest(adminRequestBody);
            case EXECUTOR_REFRESH:
                return handleExecutorRefreshRequest(adminRequestBody);
            case ALARM_MANAGE:
                return handleAlarmManageRequest(adminRequestBody);
            case LOG_MANAGE:
                return handleLogManageRequest(adminRequestBody);
            default:
                throw new IllegalArgumentException("DynamicTp admin request type "
                        + adminRequestBody.getRequestType().getValue() + " is not supported");
        }
    }

    @Override
    public String interest() {
        return AdminRequestBody.class.getName();
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    private Object handleExecutorMonitorRequest(AdminRequestBody adminRequestBody) {
        adminRequestBody.setBody(AdminCollector.getMultiPoolStats());
        return adminRequestBody;
    }

    private Object handleExecutorRefreshRequest(AdminRequestBody adminRequestBody) {
        Object properties = adminRequestBody.getBody();
        if (properties == null) {
            log.error("DynamicTp admin executor refresh failed, properties is null");
            return null;
        }
        adminRefresher.refresh((Map<Object, Object>) properties);
        return adminRequestBody;
    }

    private Object handleAlarmManageRequest(AdminRequestBody adminRequestBody) {

        return adminRequestBody;
    }

    private Object handleLogManageRequest(AdminRequestBody adminRequestBody) {

        return adminRequestBody;
    }
}
