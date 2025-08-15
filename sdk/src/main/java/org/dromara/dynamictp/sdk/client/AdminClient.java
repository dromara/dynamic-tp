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

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.Url;
import com.alipay.remoting.config.Configs;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.serialization.HessianSerializer;
import com.alipay.remoting.serialization.SerializerManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.entity.AdminRequestBody;
import org.dromara.dynamictp.common.em.AdminRequestTypeEnum;
import org.dromara.dynamictp.common.entity.AttributeRequestBody;
import org.dromara.dynamictp.sdk.client.processor.AdminClientUserProcessor;
import org.dromara.dynamictp.sdk.client.processor.AdminCloseEventProcessor;
import org.dromara.dynamictp.sdk.client.processor.AdminConnectEventProcessor;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DynamicTp admin client
 * 
 * @author eachann
 */
@Slf4j
public class AdminClient {

    private final String adminIp = "127.0.0.1";

    private final int adminPort = 8989;

    private final Url adminUrl = new Url(adminIp, adminPort);

    @Value("${dynamictp.clientName:${spring.application.name}}")
    private String clientName;

    @Getter
    private static final SnowflakeGenerator SNOWFLAKE_GENERATOR = new SnowflakeGenerator();

    private final RpcClient client = new RpcClient();

    @Getter
    private static final HessianSerializer SERIALIZER = new HessianSerializer();

    @Getter
    @Setter
    private static Connection connection;

    // Connection state management
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicInteger retryCount = new AtomicInteger(0);
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MS = 1000;

    public AdminClient(AdminClientUserProcessor adminClientUserProcessor) {
        client.addConnectionEventProcessor(ConnectionEventType.CONNECT, new AdminConnectEventProcessor(this));
        client.addConnectionEventProcessor(ConnectionEventType.CLOSE, new AdminCloseEventProcessor(this));
        client.registerUserProcessor(adminClientUserProcessor);
        client.enableReconnectSwitch();
        client.startup();
        SerializerManager.addSerializer(1, SERIALIZER);
        System.setProperty(Configs.SERIALIZER, String.valueOf(SERIALIZER));
    }

    public AdminClient(AdminClientUserProcessor adminClientUserProcessor, String clientName) {
        this(adminClientUserProcessor);
        this.clientName = clientName;
    }

    /**
     * Set client name manually
     * 
     * @param clientName the client name to set
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @PostConstruct
    public void init() {
        createConnection();
    }

    public Object requestToServer(AdminRequestTypeEnum requestType) {
        if (!ensureConnection()) {
            log.error("DynamicTp admin client cannot establish connection after retries, admin ip: {}, port: {}",
                    adminIp, adminPort);
            return null;
        }
        AttributeRequestBody attributeRequestBody = new AttributeRequestBody();
        attributeRequestBody.setAttribute("clientName", clientName);
        AdminRequestBody requestBody = new AdminRequestBody(SNOWFLAKE_GENERATOR.next(), requestType);
        Object object = null;
        try {
            client.invokeSync(connection, attributeRequestBody, 5000);
            object = client.invokeSync(connection, requestBody, 30000);
        } catch (RemotingException | InterruptedException e) {
            log.warn("DynamicTp admin client invoke failed, admin ip: {}, port: {}, exception:", adminIp, adminPort, e);
            // Mark connection as disconnected when request fails
            isConnected.set(false);
        }
        return object;
    }

    public Object requestToServer(AdminRequestBody adminRequestBody) {
        if (!ensureConnection()) {
            log.error("DynamicTp admin client cannot establish connection after retries, admin ip: {}, port: {}",
                    adminIp, adminPort);
            return null;
        }
        AttributeRequestBody attributeRequestBody = new AttributeRequestBody();
        attributeRequestBody.setAttribute("clientName", clientName);
        Object object = null;
        try {
            client.invokeSync(connection, attributeRequestBody, 5000);
            object = client.invokeSync(connection, adminRequestBody, 5000);
        } catch (RemotingException | InterruptedException e) {
            log.warn("DynamicTp admin client invoke failed, admin ip: {}, port: {}, exception:", adminIp, adminPort, e);
            // Mark connection as disconnected when request fails
            isConnected.set(false);
        }
        return object;
    }

    /**
     * Ensure connection is available, try to reconnect if not available
     * 
     * @return whether connection is available
     */
    private boolean ensureConnection() {
        // Check connection status
        if (isConnected.get() && connection != null && client.checkConnection(adminUrl.getOriginUrl(), true)) {
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
                    Thread.sleep(RETRY_DELAY_MS * currentRetry); // Incremental delay
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
            // Clean up old connection
            if (connection != null) {
                try {
                    client.closeStandaloneConnection(connection);
                } catch (Exception e) {
                    log.debug("Error closing old connection", e);
                }
                connection = null;
            }

            connection = client.createStandaloneConnection(adminUrl.getOriginUrl(), 30000);
            if (connection != null && connection.isFine()) {
                log.info("DynamicTp admin client connection created successfully, admin ip: {}, port: {}", adminIp,
                        adminPort);
                AttributeRequestBody attributeRequestBody = new AttributeRequestBody();
                attributeRequestBody.setAttribute("clientName", clientName);
                client.invokeSync(connection, attributeRequestBody, 5000);
                return true;
            } else {
                log.warn("DynamicTp admin client connection created but not fine, admin ip: {}, port: {}", adminIp,
                        adminPort);
                return false;
            }
        } catch (RemotingException | InterruptedException e) {
            log.warn("DynamicTp admin create connection failed, admin ip: {}, port: {}", adminIp, adminPort, e);
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

    public void close() {
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
