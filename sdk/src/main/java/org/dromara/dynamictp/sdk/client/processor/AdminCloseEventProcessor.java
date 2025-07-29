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

package org.dromara.dynamictp.sdk.client.processor;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventProcessor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.sdk.client.AdminClient;

/**
 * AdminCloseEventProcessor related
 *
 * @author eachann
 */
@Slf4j
public class AdminCloseEventProcessor implements ConnectionEventProcessor {

    private final AdminClient adminClient;

    public AdminCloseEventProcessor(AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    @Override
    public void onEvent(String remoteAddress, Connection connection) {
        log.info("DynamicTp admin client is disconnected, admin ip: {}, port: {}", connection.getRemoteAddress(),
                connection.getRemotePort());
        // Clean up connection object and update status when connection is closed
        AdminClient.setConnection(null);
        adminClient.updateConnectionStatus(false);
    }
}
