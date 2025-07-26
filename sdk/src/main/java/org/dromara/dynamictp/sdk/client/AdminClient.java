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
import org.dromara.dynamictp.sdk.client.processor.AdminClientUserProcessor;
import org.dromara.dynamictp.sdk.client.processor.AdminCloseEventProcessor;
import org.dromara.dynamictp.sdk.client.processor.AdminConnectEventProcessor;

@Slf4j
public class AdminClient {

    private final String adminIp = "127.0.0.1";

    private final int port = 8989;

    private final Url url = new Url(adminIp, port);

    @Getter
    private static final SnowflakeGenerator SNOWFLAKE_GENERATOR = new SnowflakeGenerator();

    private final RpcClient client = new RpcClient();

    @Getter
    private static final HessianSerializer SERIALIZER = new HessianSerializer();

    @Getter
    @Setter
    private static Connection connection;

    public AdminClient() {
        client.addConnectionEventProcessor(ConnectionEventType.CONNECT, new AdminConnectEventProcessor());
        client.addConnectionEventProcessor(ConnectionEventType.CLOSE, new AdminCloseEventProcessor());
        client.registerUserProcessor(new AdminClientUserProcessor());
        client.enableReconnectSwitch();
        client.startup();
        SerializerManager.addSerializer(1, SERIALIZER);
        System.setProperty(Configs.SERIALIZER, String.valueOf(SERIALIZER));
        createConnection();
    }

    public Object requestToServer(AdminRequestTypeEnum requestType) {
        if (!client.checkConnection(url.getOriginUrl(), true)) {
            if (!createConnection()) {
                return null;
            }
        }
        AdminRequestBody requestBody = new AdminRequestBody(SNOWFLAKE_GENERATOR.next(), requestType);

        Object object =  null;
        try {
            object = client.invokeSync(connection, requestBody, 30000);
        } catch (RemotingException | InterruptedException e) {
            log.warn("DynamicTp admin client invoke failed, admin ip: {}, port: {}, exception:", adminIp, port, e);
        }
        return object;
    }

    public Object requestToServer(AdminRequestBody adminRequestBody) {
        if (!client.checkConnection(url.getOriginUrl(), true)) {
            if (!createConnection()) {
                return null;
            }
        }
        Object object = null;
        try {
            object = client.invokeSync(connection, adminRequestBody, 5000);
        } catch (RemotingException | InterruptedException e) {
            log.warn("DynamicTp admin client invoke failed, admin ip: {}, port: {}, exception:", adminIp, port, e);
        }
        return object;
    }

    private boolean createConnection() {
        try {
            connection = client.createStandaloneConnection(url.getOriginUrl(), 30000);
        } catch (RemotingException e) {
            log.warn("DynamicTp admin create connection failed, admin ip: {}, port: {}", adminIp, port, e);
            return false;
        }
        return true;
    }

    public void close() {
        client.closeStandaloneConnection(connection);
        client.shutdown();
    }
}
