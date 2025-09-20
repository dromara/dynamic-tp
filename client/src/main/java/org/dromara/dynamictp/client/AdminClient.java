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

package org.dromara.dynamictp.client;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.config.Configs;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.serialization.HessianSerializer;
import com.alipay.remoting.serialization.SerializerManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.client.selector.RandomAdminNodeSelector;
import org.dromara.dynamictp.client.selector.WeightedRoundRobinAdminNodeSelector;
import org.dromara.dynamictp.common.entity.AdminRequestBody;
import org.dromara.dynamictp.common.em.AdminRequestTypeEnum;
import org.dromara.dynamictp.client.processor.AdminClientUserProcessor;
import org.dromara.dynamictp.client.processor.AdminCloseEventProcessor;
import org.dromara.dynamictp.client.processor.AdminConnectEventProcessor;
import org.dromara.dynamictp.client.cluster.AdminClusterManager;
import org.dromara.dynamictp.client.node.AdminNode;
import org.dromara.dynamictp.client.selector.AdminNodeSelector;
import org.dromara.dynamictp.client.selector.RoundRobinAdminNodeSelector;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DynamicTp admin client
 * 
 * @author eachann
 */
@Slf4j
public class AdminClient {

    @Value("${dynamictp.adminNodes:}")
    private String adminNodes;

    @Value("${dynamictp.loadBalanceStrategy:roundRobin}")
    private String loadBalanceStrategy;

    /**
     *
     * @param clientName the adminclient name
     */
    @Setter
    @Value("${dynamictp.clientName:${spring.application.name}}")
    private String clientName;

    /**
     *
     * @param serviceName the adminclient service name
     */
    @Setter
    @Value("${dynamictp.serviceName:${spring.application.name}}")
    private String serviceName;

    @Value("${dynamictp.adminEnabled:false}")
    private Boolean adminEnabled;

    @Getter
    private static final SnowflakeGenerator SNOWFLAKE_GENERATOR = new SnowflakeGenerator();

    private final RpcClient client = new RpcClient();

    @Getter
    private static final HessianSerializer SERIALIZER = new HessianSerializer();

    @Getter
    @Setter
    private static Connection connection;

    /**
     * 集群管理器
     */
    private AdminClusterManager clusterManager;

    /**
     * Connection state management
      */
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicInteger retryCount = new AtomicInteger(0);
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MS = 1000;

    /**
     * Heartbeat mechanism
     */
    private static final long HEARTBEAT_INTERVAL_SECONDS = 30;

    private ScheduledExecutorService heartbeatExecutor;

    public AdminClient(AdminClientUserProcessor adminClientUserProcessor) {
        this(adminClientUserProcessor, "");
    }

    public AdminClient(AdminClientUserProcessor adminClientUserProcessor, String clientName) {
        this(adminClientUserProcessor, clientName, "", "", "", false);
    }

    public AdminClient(AdminClientUserProcessor adminClientUserProcessor, String clientName, String serviceName, String adminNodes, String loadBalanceStrategy, Boolean adminEnabled) {
        if (!clientName.isEmpty()) {
            this.clientName = clientName;
        }
        if (!serviceName.isEmpty()) {
            this.serviceName = serviceName;
        }
        if (!adminNodes.isEmpty()) {
            this.adminNodes = adminNodes;
        }
        if (!loadBalanceStrategy.isEmpty()) {
            this.loadBalanceStrategy = loadBalanceStrategy;
        }
        this.adminEnabled = adminEnabled;

        client.addConnectionEventProcessor(ConnectionEventType.CONNECT, new AdminConnectEventProcessor(this));
        client.addConnectionEventProcessor(ConnectionEventType.CLOSE, new AdminCloseEventProcessor(this));
        client.registerUserProcessor(adminClientUserProcessor);
        client.enableReconnectSwitch();
        client.startup();
        SerializerManager.addSerializer(1, SERIALIZER);
        System.setProperty(Configs.SERIALIZER, String.valueOf(SERIALIZER));
    }

    @PostConstruct
    public void init() {
        try {
            initClusterManager();
            createConnection();
            startHeartbeat();
        } catch (Exception e) {
            log.error("Failed to initialize AdminClient", e);
            // 如果初始化失败，不要启动心跳，避免持续重试
            if (heartbeatExecutor != null) {
                stopHeartbeat();
            }
            throw new RuntimeException("AdminClient initialization failed", e);
        }
    }

    /**
     * 初始化集群管理器
     */
    private void initClusterManager() {
        // 创建节点选择器
        AdminNodeSelector selector = createNodeSelector();

        // 创建集群管理器
        clusterManager = new AdminClusterManager(selector);

        // 添加配置的节点
        addConfiguredNodes();

        log.info("Admin cluster manager initialized with {} nodes", clusterManager.getAllNodes().size());
    }

    /**
     * 创建节点选择器
     */
    private AdminNodeSelector createNodeSelector() {
        switch (loadBalanceStrategy.toLowerCase()) {
            case "random":
                return new RandomAdminNodeSelector();
            case "weighted":
                return new WeightedRoundRobinAdminNodeSelector();
            case "roundRobin":
            default:
                return new RoundRobinAdminNodeSelector();
        }
    }

        /**
     * 添加配置的节点
     */
    private void addConfiguredNodes() {
        log.info("Configuring admin nodes: adminNodes={}", adminNodes);
        
        if (adminNodes == null || adminNodes.trim().isEmpty()) {
            log.error("No admin nodes configured. Please configure dynamictp.adminNodes property.");
            throw new IllegalStateException("No admin nodes configured");
        }
        
        String[] nodeConfigs = adminNodes.split(",");
        log.info("Parsed {} node configurations", nodeConfigs.length);
        
        for (String nodeConfig : nodeConfigs) {
            String trimmedConfig = nodeConfig.trim();
            if (trimmedConfig.isEmpty()) {
                log.warn("Skipping empty node configuration");
                continue;
            }
            
            String[] parts = trimmedConfig.split(":");
            if (parts.length >= 2) {
                try {
                    String ip = parts[0].trim();
                    int port = Integer.parseInt(parts[1].trim());
                    int weight = parts.length > 2 ? Integer.parseInt(parts[2].trim()) : 1;
                    
                    if (ip.isEmpty()) {
                        log.error("Invalid IP address in node configuration: {}", trimmedConfig);
                        throw new IllegalArgumentException("Invalid IP address in node configuration: " + trimmedConfig);
                    }
                    
                    clusterManager.addNode(ip, port, weight);
                    log.info("Added admin node: {}:{} with weight {}", ip, port, weight);
                } catch (NumberFormatException e) {
                    log.error("Invalid admin node configuration: {}", trimmedConfig, e);
                    throw new IllegalArgumentException("Invalid admin node configuration: " + trimmedConfig, e);
                }
            } else {
                log.error("Invalid admin node configuration format: {}", trimmedConfig);
                throw new IllegalArgumentException("Invalid admin node configuration format: " + trimmedConfig);
            }
        }
        
        if (clusterManager.getAllNodes().isEmpty()) {
            log.error("No valid admin nodes found in configuration");
            throw new IllegalStateException("No valid admin nodes found in configuration");
        }
        
        log.info("Successfully configured {} admin nodes", clusterManager.getAllNodes().size());
    }

    /**
     * Start heartbeat mechanism if admin is enabled
     */
    private void startHeartbeat() {
        if (adminEnabled) {
            heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "DynamicTp-AdminClient-Heartbeat");
                thread.setDaemon(true);
                return thread;
            });

            heartbeatExecutor.scheduleAtFixedRate(() -> {
                try {
                    ensureConnection();
                } catch (Exception e) {
                    log.warn("DynamicTp admin client heartbeat execution failed", e);
                }
            }, HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    /**
     * Stop heartbeat mechanism
     */
    private void stopHeartbeat() {
        if (heartbeatExecutor != null && !heartbeatExecutor.isShutdown()) {
            try {
                heartbeatExecutor.shutdown();
                if (!heartbeatExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    heartbeatExecutor.shutdownNow();
                }
                log.info("DynamicTp admin client heartbeat stopped");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                heartbeatExecutor.shutdownNow();
                log.warn("DynamicTp admin client heartbeat shutdown interrupted");
            }
        }
    }

    public Object requestToServer(AdminRequestTypeEnum requestType) {
        if (!ensureConnection()) {
            log.error("DynamicTp admin client cannot establish connection after retries");
            return null;
        }
        AdminRequestBody requestBody = new AdminRequestBody(SNOWFLAKE_GENERATOR.next(), requestType);
        requestBody.setAttributes("clientName", clientName);
        requestBody.setAttributes("serviceName", serviceName);
        Object object = null;
        try {
            object = client.invokeSync(connection, requestBody, 30000);
            // 标记当前节点成功
            AdminNode currentNode = getCurrentNode();
            if (currentNode != null) {
                clusterManager.markNodeSuccess(currentNode);
            }
        } catch (RemotingException | InterruptedException e) {
            log.warn("DynamicTp admin client invoke failed, exception:", e);
            // 标记当前节点失败
            AdminNode currentNode = getCurrentNode();
            if (currentNode != null) {
                clusterManager.markNodeFailed(currentNode);
            }
            isConnected.set(false);
        }
        if (object instanceof IllegalStateException) {
            log.error(((IllegalStateException) object).getMessage());
            return null;
        }
        return object;
    }

    public Object requestToServer(AdminRequestBody requestBody) {
        if (!ensureConnection()) {
            log.error("DynamicTp admin client cannot establish connection after retries");
            return null;
        }
        requestBody.setAttributes("clientName", clientName);
        requestBody.setAttributes("serviceName", serviceName);
        Object object = null;
        try {
            object = client.invokeSync(connection, requestBody, 5000);
            // 标记当前节点成功
            AdminNode currentNode = getCurrentNode();
            if (currentNode != null) {
                clusterManager.markNodeSuccess(currentNode);
            }
        } catch (RemotingException | InterruptedException e) {
            log.warn("DynamicTp admin client invoke failed, exception:", e);
            // 标记当前节点失败
            AdminNode currentNode = getCurrentNode();
            if (currentNode != null) {
                clusterManager.markNodeFailed(currentNode);
            }
            // Mark connection as disconnected when request fails
            isConnected.set(false);
        }
        if (object instanceof IllegalStateException) {
            log.error(((IllegalStateException) object).getMessage());
            return null;
        }
        return object;
    }

    /**
     * 获取当前连接的节点
     */
    private AdminNode getCurrentNode() {
        if (connection != null) {
            String address = connection.getRemoteAddress().getAddress().getHostAddress();
            int port = connection.getRemoteAddress().getPort();

            for (AdminNode node : clusterManager.getAllNodes()) {
                if (node.getIp().equals(address) && node.getPort() == port) {
                    return node;
                }
            }
        }
        return null;
    }

    /**
     * Ensure connection is available, try to reconnect if not available
     * 
     * @return whether connection is available
     */
    private boolean ensureConnection() {
        // Check connection status
        if (isConnected.get() && connection != null && connection.isFine()) {
            return true;
        }

        // Connection not available, try to reconnect
        return reconnectWithRetry();
    }

    /**
     * Reconnect with retry mechanism
     * 
     * @return whether reconnection is successful
     */
    private boolean reconnectWithRetry() {
        int currentRetry = 0;
        while (currentRetry < MAX_RETRY_COUNT) {
            log.info("DynamicTp admin client attempting to reconnect, attempt: {}/{}", currentRetry + 1,
                    MAX_RETRY_COUNT);

            if (createConnection()) {
                isConnected.set(true);
                retryCount.set(0);
                log.info("DynamicTp admin client reconnected successfully");
                return true;
            }

            currentRetry++;
            retryCount.incrementAndGet();

            if (currentRetry < MAX_RETRY_COUNT) {
                try {
                    // Incremental delay
                    Thread.sleep(RETRY_DELAY_MS * currentRetry);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("DynamicTp admin client reconnection interrupted");
                    return false;
                }
            }
        }

        log.error("DynamicTp admin client failed to reconnect after {} attempts", MAX_RETRY_COUNT);
        return false;
    }

    private boolean createConnection() {
        try {
            // 检查集群管理器是否已初始化
            if (clusterManager == null) {
                log.error("Cluster manager not initialized");
                return false;
            }
            
            // Clean up old connection
            if (connection != null) {
                try {
                    client.closeStandaloneConnection(connection);
                } catch (Exception e) {
                    log.debug("Error closing old connection", e);
                }
                connection = null;
            }

            AdminNode selectedNode = clusterManager.selectNode(null);
            if (selectedNode == null) {
                log.error("No available admin nodes for connection");
                return false;
            }

            connection = client.createStandaloneConnection(selectedNode.getAddress(), 30000);
            if (connection != null && connection.isFine()) {
                log.info("DynamicTp admin client connection created successfully, admin node: {}",
                        selectedNode.getAddress());
                AdminRequestBody requestBody = new AdminRequestBody(SNOWFLAKE_GENERATOR.next(),
                        AdminRequestTypeEnum.EXECUTOR_REFRESH);
                requestBody.setAttributes("clientName", clientName);
                requestBody.setAttributes("serviceName", serviceName);
                Object object = client.invokeSync(connection, requestBody, 5000);
                if (object instanceof IllegalStateException) {
                    log.error(((IllegalStateException) object).getMessage());
                    client.closeStandaloneConnection(connection);
                    return false;
                }
                return true;
            } else {
                log.warn("DynamicTp admin client connection created but not fine, admin node: {}",
                        selectedNode.getAddress());
                return false;
            }
        } catch (RemotingException | InterruptedException e) {
            log.error("DynamicTp admin create connection failed", e);
            return false;
        }
    }

    /**
     * Get current connection status
     * 
     * @return whether connection is available
     */
    public boolean isConnected() {
        return isConnected.get() && connection != null && connection.isFine();
    }

    /**
     * Get retry count
     * 
     * @return retry count
     */
    public int getRetryCount() {
        return retryCount.get();
    }

    /**
     * Update connection status
     * 
     * @param connected whether connected
     */
    public void updateConnectionStatus(boolean connected) {
        isConnected.set(connected);
        if (!connected) {
            retryCount.incrementAndGet();
        }
    }

    /**
     * 获取集群管理器
     */
    public AdminClusterManager getClusterManager() {
        return clusterManager;
    }

    /**
     * 获取所有admin节点
     */
    public List<AdminNode> getAllAdminNodes() {
        return clusterManager != null ? clusterManager.getAllNodes() : null;
    }

    /**
     * 获取健康的admin节点
     */
    public List<AdminNode> getHealthyAdminNodes() {
        return clusterManager != null ? clusterManager.getHealthyNodes() : null;
    }

    @PreDestroy
    public void close() {
        stopHeartbeat();
        if (clusterManager != null) {
            clusterManager.shutdown();
        }
        isConnected.set(false);
        if (connection != null) {
            try {
                client.closeStandaloneConnection(connection);
            } catch (Exception e) {
                log.warn("Error closing connection", e);
            }
            connection = null;
        }
        client.shutdown();
    }
}
