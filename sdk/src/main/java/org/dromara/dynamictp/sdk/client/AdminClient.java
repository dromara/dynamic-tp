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

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.sdk.client.processor.AdminClientUserProcessor;
import org.dromara.dynamictp.sdk.client.processor.AdminCloseEventProcessor;
import org.dromara.dynamictp.sdk.client.processor.AdminConnectEventProcessor;

/**
 * AdminClient related
 *
 * @author eachannchan
 */
@Slf4j
public class AdminClient extends RpcClient {

    private final String adminIp = "127.0.0.1";

    private final int port = 8989;

    private Connection connection;


    public AdminClient() {
        super();
        super.addConnectionEventProcessor(ConnectionEventType.CONNECT, new AdminConnectEventProcessor());
        super.addConnectionEventProcessor(ConnectionEventType.CLOSE, new AdminCloseEventProcessor());
        super.registerUserProcessor(new AdminClientUserProcessor());
        super.enableReconnectSwitch();
        super.startup();
        log.info("DynamicTp admin client started, admin ip: {}, port: {}", adminIp, port);
        try {
            connection = super.createStandaloneConnection(adminIp, port, 30000);
        } catch (RemotingException e) {
            log.info("DynamicTp admin client is not connected, admin ip: {}, port: {}", adminIp, port);
        } finally {
            super.closeStandaloneConnection(connection);
            super.shutdown();
        }
    }

    public Object requestToServer(AdminRequestTypeEnum requestType, Object body) throws RemotingException, InterruptedException {
        AdminRequestBody requestBody = new AdminRequestBody(requestType, body);
        return super.invokeSync(connection, requestBody, 30000);
    }

}
