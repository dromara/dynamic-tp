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

package org.dromara.dynamictp.sdk.client;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class AdminClientUserProcessor extends SyncUserProcessor<AdminRequestBody> {

    private final ExecutorService executor;

    @Getter
    private String remoteAddress = "NOT-CONNECT";

    public AdminClientUserProcessor() {
        this.executor = Executors.newSingleThreadExecutor();
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
                throw new IllegalArgumentException("DynamicTp admin request type " + adminRequestBody.getRequestType().getValue() + " is not supported");
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
        return null;
    }

    private Object handleExecutorRefreshRequest(AdminRequestBody adminRequestBody) {
        return null;
    }

    private Object handleAlarmManageRequest(AdminRequestBody adminRequestBody) {
        return null;
    }

    private Object handleLogManageRequest(AdminRequestBody adminRequestBody) {
        return null;
    }

}
