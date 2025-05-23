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

package org.dromara.dynamictp.adapter.thrift;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.support.proxy.ThreadPoolExecutorProxy;
import org.dromara.dynamictp.jvmti.JVMTI;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ThriftDtpAdapter for managing Thrift server thread pools
 *
 * @author devin
 * @since 1.1.5
 */
@Slf4j
@SuppressWarnings("all")
public class ThriftDtpAdapter extends AbstractDtpAdapter {

    private static final String TP_PREFIX = "thriftTp";
    
    private static final String THREAD_POOL_SERVER_EXECUTOR_FIELD = "executorService_";
    private static final String HSHASERVER_EXECUTOR_FIELD = "invoker";
    private static final String THREADED_SELECTOR_WORKER_FIELD = "invoker";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(dtpProperties.getThriftTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected String getTpPrefix() {
        return TP_PREFIX;
    }
    
    private String genTpName(String serverType, int port) {
        return TP_PREFIX + "#" + serverType + "#" + (port > 0 ? port : "");
    }

    @Override
    protected void initialize() {
        super.initialize();
        
        List<TThreadPoolServer> tThreadPoolServers = JVMTI.getInstances(TThreadPoolServer.class);
        if (CollectionUtils.isEmpty(tThreadPoolServers)) {
            log.warn("Cannot find instances of TThreadPoolServer.");
        } else {
            tThreadPoolServers.forEach(this::initializeTThreadPoolServer);
        }
        
        List<THsHaServer> tHsHaServers = JVMTI.getInstances(THsHaServer.class);
        if (CollectionUtils.isEmpty(tHsHaServers)) {
            log.warn("Cannot find instances of THsHaServer.");
        } else {
            tHsHaServers.forEach(this::initializeTHsHaServer);
        }
        
        List<TThreadedSelectorServer> tThreadedSelectorServers = JVMTI.getInstances(TThreadedSelectorServer.class);
        if (CollectionUtils.isEmpty(tThreadedSelectorServers)) {
            log.warn("Cannot find instances of TThreadedSelectorServer.");
        } else {
            tThreadedSelectorServers.forEach(this::initializeTThreadedSelectorServer);
        }
    }
    
    public void initializeTThreadPoolServer(TThreadPoolServer server) {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(
                TThreadPoolServer.class, THREAD_POOL_SERVER_EXECUTOR_FIELD, server);
        if (Objects.nonNull(executor)) {
            int port = getServerPort(server);
            String tpName = genTpName("TThreadPoolServer", port);
            ThreadPoolExecutorProxy proxy = new ThreadPoolExecutorProxy(executor);
            enhanceOriginExecutorWithoutFinalize(tpName, proxy, THREAD_POOL_SERVER_EXECUTOR_FIELD, server);
            log.info("DynamicTp adapter, thrift TThreadPoolServer executorService_ enhanced, tpName: {}", tpName);
        }
    }
    
    public void initializeTHsHaServer(THsHaServer server) {
        ExecutorService executor = (ExecutorService) ReflectionUtil.getFieldValue(
                THsHaServer.class, HSHASERVER_EXECUTOR_FIELD, server);
        if (Objects.nonNull(executor) && executor instanceof ThreadPoolExecutor) {
            int port = getServerPort(server);
            String tpName = genTpName("THsHaServer", port);
            ThreadPoolExecutorProxy proxy = new ThreadPoolExecutorProxy((ThreadPoolExecutor) executor);
            enhanceOriginExecutorWithoutFinalize(tpName, proxy, HSHASERVER_EXECUTOR_FIELD, server);
            log.info("DynamicTp adapter, thrift THsHaServer invoker enhanced, tpName: {}", tpName);
        }
    }
    
    public void initializeTThreadedSelectorServer(TThreadedSelectorServer server) {
        ExecutorService executor = (ExecutorService) ReflectionUtil.getFieldValue(
                TThreadedSelectorServer.class, THREADED_SELECTOR_WORKER_FIELD, server);
        if (Objects.nonNull(executor) && executor instanceof ThreadPoolExecutor) {
            int port = getServerPort(server);
            String tpName = genTpName("TThreadedSelectorServer", port);
            ThreadPoolExecutorProxy proxy = new ThreadPoolExecutorProxy((ThreadPoolExecutor) executor);
            enhanceOriginExecutorWithoutFinalize(tpName, proxy, THREADED_SELECTOR_WORKER_FIELD, server);
            log.info("DynamicTp adapter, thrift TThreadedSelectorServer invoker enhanced, tpName: {}", tpName);
        }
    }
    
    /**
     * Try to get the server port for better naming
     * 
     * @param server Thrift server instance
     * @return port number or -1 if not available
     */
    private int getServerPort(Object server) {
        try {
            Object serverTransport = ReflectionUtil.getFieldValue("serverTransport_", server);
            if (serverTransport != null) {
                Object serverSocket = ReflectionUtil.getFieldValue("serverSocket_", serverTransport);
                if (serverSocket != null) {
                    Object localPort = ReflectionUtil.getFieldValue("port_", serverSocket);
                    if (localPort instanceof Integer) {
                        return (Integer) localPort;
                    }
                }
            }
            
            Object transport = ReflectionUtil.getFieldValue("inputTransport_", server);
            if (transport != null) {
                Object socket = ReflectionUtil.getFieldValue("serverSocket_", transport);
                if (socket != null) {
                    Object localPort = ReflectionUtil.getFieldValue("port_", socket);
                    if (localPort instanceof Integer) {
                        return (Integer) localPort;
                    }
                }
            }
            
            log.debug("Could not extract port from Thrift server: {}", server.getClass().getSimpleName());
        } catch (Exception e) {
            log.debug("Error extracting port from Thrift server: {}", e.getMessage());
        }
        return -1;
    }
}
