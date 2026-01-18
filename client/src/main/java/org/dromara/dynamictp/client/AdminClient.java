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
import org.dromara.dynamictp.client.cluster.AdminClusterManager;
import org.dromara.dynamictp.client.cluster.AdminNode;
import org.dromara.dynamictp.client.processor.ClientUserProcessor;
import org.dromara.dynamictp.client.processor.CloseEventProcessor;
import org.dromara.dynamictp.client.processor.ConnectEventProcessor;
import org.dromara.dynamictp.client.loadbalance.NodeSelector;
import org.dromara.dynamictp.client.loadbalance.RandomNodeSelector;
import org.dromara.dynamictp.client.loadbalance.RoundRobinNodeSelector;
import org.dromara.dynamictp.client.loadbalance.WeightedRoundRobinNodeSelector;
import org.dromara.dynamictp.common.entity.RpcRequest;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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

    @Setter
    @Value("${dynamictp.clientName:${spring.application.name}}")
    private String clientName;

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

    /**
     * Use AtomicReference to ensure thread safety
     */
    private static final AtomicReference<Connection> CONNECTION_REF = new AtomicReference<>();

    public static Connection getConnection() {
        return CONNECTION_REF.get();
    }

    public static void setConnection(Connection connection) {
        CONNECTION_REF.set(connection);
    }

    /**
     * Cluster manager
     */
    private AdminClusterManager clusterManager;

    /**
     * Connection state management
     */
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicInteger retryCount = new AtomicInteger(0);

    private ScheduledExecutorService heartbeatExecutor;

    public AdminClient(ClientUserProcessor clientUserProcessor) {
        this(clientUserProcessor, "");
    }

    public AdminClient(ClientUserProcessor clientUserProcessor, String clientName) {
        this(clientUserProcessor, clientName, "", "", "", false);
    }

    public AdminClient(ClientUserProcessor clientUserProcessor, String clientName, String serviceName, String adminNodes, String loadBalanceStrategy, Boolean adminEnabled) {
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

        client.addConnectionEventProcessor(ConnectionEventType.CONNECT, new ConnectEventProcessor(this));
        client.addConnectionEventProcessor(ConnectionEventType.CLOSE, new CloseEventProcessor(this));
        client.registerUserProcessor(clientUserProcessor);
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
            // If initialization fails, don't start heartbeat to avoid continuous retries
            if (heartbeatExecutor != null) {
                stopHeartbeat();
            }
            throw new RuntimeException("AdminClient initialization failed", e);
        }
    }

    /**
     * Initialize cluster manager
     */
    private void initClusterManager() {
        // Create node selector
        NodeSelector selector = createNodeSelector();

        // Create cluster manager
        clusterManager = new AdminClusterManager(selector);

        // Add configured nodes
        addConfiguredNodes();

        log.info("Admin cluster manager initialized with {} nodes", clusterManager.getAllNodes().size());
    }

    /**
     * Create node selector
     */
    private NodeSelector createNodeSelector() {
        String strategy = loadBalanceStrategy.toLowerCase();
        switch (strategy) {
            case AdminClientConstants.STRATEGY_RANDOM:
                return new RandomNodeSelector();
            case AdminClientConstants.STRATEGY_WEIGHTED_ROUND_ROBIN:
                return new WeightedRoundRobinNodeSelector();
            case AdminClientConstants.STRATEGY_ROUND_ROBIN:
            default:
                return new RoundRobinNodeSelector();
        }
    }

    /**
     * Add configured nodes
     */
    private void addConfiguredNodes() {
        log.info("Configuring admin nodes: adminNodes={}", adminNodes);
        
        validateAdminNodesConfig();
        
        String[] nodeConfigs = adminNodes.split(",");
        log.info("Parsed {} node configurations", nodeConfigs.length);
        
        for (String nodeConfig : nodeConfigs) {
            parseAndAddNode(nodeConfig);
        }
        
        if (clusterManager.getAllNodes().isEmpty()) {
            log.error("No valid admin nodes found in configuration");
            throw new IllegalStateException("No valid admin nodes found in configuration");
        }
        
        log.info("Successfully configured {} admin nodes", clusterManager.getAllNodes().size());
    }

    /**
     * Validate admin nodes configuration
     */
    private void validateAdminNodesConfig() {
        if (adminNodes == null || adminNodes.trim().isEmpty()) {
            log.error("No admin nodes configured. Please configure dynamictp.adminNodes property.");
            throw new IllegalStateException("No admin nodes configured");
        }
    }

    /**
     * Parse and add a single node configuration
     * 
     * @param nodeConfig node configuration string, format: ip:port[:weight]
     */
    private void parseAndAddNode(String nodeConfig) {
        String trimmedConfig = nodeConfig.trim();
        if (trimmedConfig.isEmpty()) {
            log.warn("Skipping empty node configuration");
            return;
        }
        
        String[] parts = trimmedConfig.split(":");
        if (parts.length < 2) {
            log.error("Invalid admin node configuration format: {}", trimmedConfig);
            throw new IllegalArgumentException("Invalid admin node configuration format: " + trimmedConfig);
        }
        
        try {
            String ip = parts[0].trim();
            int port = Integer.parseInt(parts[1].trim());
            int weight = parts.length > 2 ? Integer.parseInt(parts[2].trim()) : 1;
            
            if (ip.isEmpty()) {
                log.error("Invalid IP address in node configuration: {}", trimmedConfig);
                throw new IllegalArgumentException("Invalid IP address in node configuration: " + trimmedConfig);
            }
            
            clusterManager.addNode(ip, port, weight);
            log.debug("Added admin node: {}:{} with weight {}", ip, port, weight);
        } catch (NumberFormatException e) {
            log.error("Invalid admin node configuration: {}", trimmedConfig, e);
            throw new IllegalArgumentException("Invalid admin node configuration: " + trimmedConfig, e);
        }
    }

    /**
     * Start heartbeat mechanism (if admin feature is enabled)
     */
    private void startHeartbeat() {
        if (adminEnabled) {
            heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, AdminClientConstants.THREAD_NAME_HEARTBEAT);
                thread.setDaemon(true);
                return thread;
            });

            long interval = AdminClientConstants.HEARTBEAT_INTERVAL_SECONDS;
            heartbeatExecutor.scheduleAtFixedRate(() -> {
                try {
                    ensureConnection();
                } catch (Exception e) {
                    log.warn("DynamicTp admin client heartbeat execution failed", e);
                }
            }, interval, interval, TimeUnit.SECONDS);
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

    /**
     * Send request to server
     *
     * @param requestType request type
     * @return response result
     */
    public Object requestToServer(String requestType) {
        RpcRequest requestBody = new RpcRequest(SNOWFLAKE_GENERATOR.next(), requestType);
        return doRequest(requestBody, AdminClientConstants.DEFAULT_REQUEST_TIMEOUT_MS);
    }

    /**
     * Send request to server
     *
     * @param requestBody request body
     * @return response result
     */
    public Object requestToServer(RpcRequest requestBody) {
        return doRequest(requestBody, AdminClientConstants.QUICK_REQUEST_TIMEOUT_MS);
    }

    /**
     * Core method for executing requests
     *
     * @param requestBody request body
     * @param timeoutMs   timeout in milliseconds
     * @return response result
     */
    private Object doRequest(RpcRequest requestBody, int timeoutMs) {
        if (!ensureConnection()) {
            log.error("DynamicTp admin client cannot establish connection after retries");
            return null;
        }
        
        Connection connection = CONNECTION_REF.get();
        requestBody.addAttribute("clientName", clientName);
        requestBody.addAttribute("serviceName", serviceName);
        
        Object result = null;
        try {
            result = client.invokeSync(connection, requestBody, timeoutMs);
            // Mark current node as success
            markCurrentNodeStatus(true);
        } catch (RemotingException | InterruptedException e) {
            log.warn("DynamicTp admin client invoke failed, exception:", e);
            // Mark current node as failed
            markCurrentNodeStatus(false);
            isConnected.set(false);
        }
        
        if (result instanceof IllegalStateException) {
            log.error(((IllegalStateException) result).getMessage());
            return null;
        }
        return result;
    }

    /**
     * Mark current node status
     *
     * @param success whether success
     */
    private void markCurrentNodeStatus(boolean success) {
        AdminNode currentNode = getCurrentNode();
        if (currentNode != null) {
            if (success) {
                clusterManager.markNodeSuccess(currentNode);
            } else {
                clusterManager.markNodeFailed(currentNode);
            }
        }
    }

    /**
     * Get current connected node
     */
    private AdminNode getCurrentNode() {
        Connection connection = CONNECTION_REF.get();
        if (connection != null && connection.getRemoteAddress() != null) {
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
        Connection connection = CONNECTION_REF.get();
        if (isConnected.get() && connection != null && connection.isFine()) {
            // Send health check message to server
            RpcRequest requestBody = new RpcRequest(SNOWFLAKE_GENERATOR.next(), AdminClientConstants.REQUEST_TYPE_EXECUTOR_MONITOR);
            requestBody.addAttribute("clientName", clientName);
            requestBody.addAttribute("serviceName", serviceName);
            try {
                client.invokeSync(connection, requestBody, AdminClientConstants.DEFAULT_REQUEST_TIMEOUT_MS);
            } catch (RemotingException | InterruptedException e) {
                log.warn("DynamicTp admin client invoke failed, exception:", e);
                markCurrentNodeStatus(false);
                isConnected.set(false);
            }
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
        int maxRetry = AdminClientConstants.CONNECTION_MAX_RETRY_COUNT;
        while (currentRetry < maxRetry) {
            log.info("DynamicTp admin client attempting to reconnect, attempt: {}/{}", currentRetry + 1,
                    maxRetry);

            if (createConnection()) {
                isConnected.set(true);
                retryCount.set(0);
                log.info("DynamicTp admin client reconnected successfully");
                return true;
            }

            currentRetry++;
            retryCount.incrementAndGet();

            if (currentRetry < maxRetry) {
                try {
                    // Incremental delay
                    Thread.sleep(AdminClientConstants.CONNECTION_RETRY_DELAY_MS * currentRetry);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("DynamicTp admin client reconnection interrupted");
                    return false;
                }
            }
        }

        log.error("DynamicTp admin client failed to reconnect after {} attempts", maxRetry);
        return false;
    }

    private boolean createConnection() {
        try {
            // Check if cluster manager is initialized
            if (clusterManager == null) {
                log.error("Cluster manager not initialized");
                return false;
            }
            
            // Clean up old connection
            Connection oldConnection = CONNECTION_REF.get();
            if (oldConnection != null) {
                try {
                    client.closeStandaloneConnection(oldConnection);
                } catch (Exception e) {
                    log.debug("Error closing old connection", e);
                }
                CONNECTION_REF.set(null);
            }

            AdminNode selectedNode = clusterManager.selectNode(null);
            if (selectedNode == null) {
                log.error("No available admin nodes for connection");
                return false;
            }

            Connection newConnection = client.createStandaloneConnection(selectedNode.getAddress(), AdminClientConstants.CONNECTION_TIMEOUT_MS);
            if (newConnection != null && newConnection.isFine()) {
                CONNECTION_REF.set(newConnection);
                log.info("DynamicTp admin client connection created successfully, admin node: {}",
                        selectedNode.getAddress());
                RpcRequest requestBody = new RpcRequest(SNOWFLAKE_GENERATOR.next(),
                        AdminClientConstants.REQUEST_TYPE_EXECUTOR_REFRESH);
                requestBody.addAttribute("clientName", clientName);
                requestBody.addAttribute("serviceName", serviceName);
                Object object = client.invokeSync(newConnection, requestBody, AdminClientConstants.QUICK_REQUEST_TIMEOUT_MS);
                if (object instanceof IllegalStateException) {
                    log.error(((IllegalStateException) object).getMessage());
                    client.closeStandaloneConnection(newConnection);
                    CONNECTION_REF.set(null);
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
        Connection connection = CONNECTION_REF.get();
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
     * Get cluster manager
     *
     * @return cluster manager
     */
    public AdminClusterManager getClusterManager() {
        return clusterManager;
    }

    /**
     * Get all admin nodes
     *
     * @return list of all admin nodes, never null
     */
    public List<AdminNode> getAllAdminNodes() {
        return clusterManager != null ? clusterManager.getAllNodes() : Collections.emptyList();
    }

    /**
     * Get healthy admin nodes
     *
     * @return list of healthy admin nodes, never null
     */
    public List<AdminNode> getHealthyAdminNodes() {
        return clusterManager != null ? clusterManager.getHealthyNodes() : Collections.emptyList();
    }

    @PreDestroy
    public void close() {
        stopHeartbeat();
        if (clusterManager != null) {
            clusterManager.shutdown();
        }
        isConnected.set(false);
        Connection connection = CONNECTION_REF.get();
        if (connection != null) {
            try {
                client.closeStandaloneConnection(connection);
            } catch (Exception e) {
                log.warn("Error closing connection", e);
            }
            CONNECTION_REF.set(null);
        }
        client.shutdown();
    }
}
