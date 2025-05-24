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

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.dromara.dynamictp.example.thrift.service.SimpleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ThriftServerService related
 *
 * @author devin
 * @since 1.1.5
 */
@Service
public class ThriftServerService implements SimpleService.Iface {

    @Value("${thrift.server.port:9998}")
    private int serverPort;

    private TThreadPoolServer server;
    private ExecutorService executorService;

    @PostConstruct
    public void start() {
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                TServerSocket serverTransport = new TServerSocket(serverPort);
                TProcessor processor = new SimpleService.Processor<>(this);
                TThreadPoolServer.Args args = new TThreadPoolServer.Args(serverTransport)
                        .processor(processor)
                        .protocolFactory(new TBinaryProtocol.Factory());
                server = new TThreadPoolServer(args);
                System.out.println("Starting Thrift server on port " + serverPort);
                server.serve();
            } catch (TTransportException e) {
                e.printStackTrace();
            }
        });
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.stop();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Override
    public String sayHello(String name) {
        return "Hello ==> " + name;
    }
}
