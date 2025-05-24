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

package org.dromara.dynamictp.example.thrift;

import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.dromara.dynamictp.example.thrift.service.SimpleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * ThriftClientService related
 *
 * @author devin
 * @since 1.1.5
 */
@Service
@Slf4j
public class ThriftClientService {

    @Value("${thrift.server.port:9998}")
    private int serverPort;

    @Value("${thrift.server.host:localhost}")
    private String serverHost;

    public String sendMessage(final String name) {
        try (TTransport transport = new TSocket(serverHost, serverPort)) {
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            SimpleService.Client client = new SimpleService.Client(protocol);
            return client.sayHello(name);
        } catch (TException e) {
            log.error("Request failed", e);
            return "FAILED with " + e.getMessage();
        }
    }
}
