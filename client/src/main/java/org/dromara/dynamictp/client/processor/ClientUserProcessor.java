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

package org.dromara.dynamictp.client.processor;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.client.AdminClientConstants;
import org.dromara.dynamictp.client.monitor.StatsCollector;
import org.dromara.dynamictp.client.refresh.ConfigRefresher;
import org.dromara.dynamictp.common.entity.RpcRequest;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Client user processor for handling admin requests
 *
 * @author eachann
 */
@Slf4j
public class ClientUserProcessor extends SyncUserProcessor<RpcRequest> implements DisposableBean {

    private final ExecutorService executor;

    @Getter
    private String remoteAddress = "NOT-CONNECT";

    private ConfigRefresher configRefresher;

    @Setter
    @Value("${dynamictp.clientName:${spring.application.name}}")
    private String clientName;

    @Setter
    @Value("${dynamictp.serviceName:${spring.application.name}}")
    private String serviceName;

    public ClientUserProcessor() {
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Autowired
    public ClientUserProcessor(ConfigRefresher configRefresher) {
        this.executor = Executors.newSingleThreadExecutor();
        this.configRefresher = configRefresher;
    }

    @Override
    public Object handleRequest(BizContext bizContext, RpcRequest request) {
        log.info("DynamicTp admin request received:{}", request.getRequestType());
        if (bizContext.isRequestTimeout()) {
            log.warn("DynamicTp admin request timeout:{}s", bizContext.getClientTimeout());
        }
        request.addAttribute("clientName", clientName);
        request.addAttribute("serviceName", serviceName);
        this.remoteAddress = bizContext.getRemoteAddress();
        return doHandleRequest(request);
    }

    private Object doHandleRequest(RpcRequest request) {
        String requestType = request.getRequestType();
        if (AdminClientConstants.REQUEST_TYPE_EXECUTOR_MONITOR.equals(requestType)) {
            return handleExecutorMonitorRequest(request);
        } else if (AdminClientConstants.REQUEST_TYPE_EXECUTOR_REFRESH.equals(requestType)) {
            return handleExecutorRefreshRequest(request);
        } else if (AdminClientConstants.REQUEST_TYPE_ALARM_MANAGE.equals(requestType)) {
            return handleAlarmManageRequest(request);
        } else if (AdminClientConstants.REQUEST_TYPE_LOG_MANAGE.equals(requestType)) {
            return handleLogManageRequest(request);
        } else {
            throw new IllegalArgumentException("DynamicTp admin request type " + requestType + " is not supported");
        }
    }

    @Override
    public String interest() {
        return RpcRequest.class.getName();
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    private Object handleExecutorMonitorRequest(RpcRequest request) {
        request.setBody(StatsCollector.getMultiPoolStats());
        return request;
    }

    @SuppressWarnings("unchecked")
    private Object handleExecutorRefreshRequest(RpcRequest request) {
        Object properties = request.getBody();
        if (properties == null) {
            log.error("DynamicTp admin executor refresh failed, properties is null");
            return null;
        }
        if (!(properties instanceof Map)) {
            log.error("DynamicTp admin executor refresh failed, properties is not a Map, actual type: {}", 
                    properties.getClass().getName());
            return null;
        }
        configRefresher.refresh((Map<Object, Object>) properties);
        return request;
    }

    /**
     * Handle alarm management request
     * TODO: Alarm management feature to be implemented
     */
    private Object handleAlarmManageRequest(RpcRequest request) {
        log.debug("Alarm manage request received, not implemented yet");
        return request;
    }

    /**
     * Handle log management request
     * TODO: Log management feature to be implemented
     */
    private Object handleLogManageRequest(RpcRequest request) {
        log.debug("Log manage request received, not implemented yet");
        return request;
    }

    @Override
    public void destroy() {
        if (executor != null && !executor.isShutdown()) {
            try {
                executor.shutdown();
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
                log.info("ClientUserProcessor executor shutdown successfully");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
                log.warn("ClientUserProcessor executor shutdown interrupted");
            }
        }
    }
}
