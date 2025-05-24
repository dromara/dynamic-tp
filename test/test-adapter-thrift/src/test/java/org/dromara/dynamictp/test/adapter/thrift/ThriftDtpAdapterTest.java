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

package org.dromara.dynamictp.test.adapter.thrift;

import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.dromara.dynamictp.adapter.thrift.ThriftDtpAdapter;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.proxy.ThreadPoolExecutorProxy;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ThriftDtpAdapter test
 *
 * @author devin
 */
@RunWith(MockitoJUnitRunner.class)
public class ThriftDtpAdapterTest {

    private ThriftDtpAdapter thriftDtpAdapter;

    @Mock
    private TThreadPoolServer tThreadPoolServer;

    @Mock
    private THsHaServer tHsHaServer;

    @Mock
    private TThreadedSelectorServer tThreadedSelectorServer;

    private ThreadPoolExecutor threadPoolExecutor;
    
    private static final String THREAD_POOL_SERVER_EXECUTOR_FIELD = "executorService_";
    private static final String HSHASERVER_EXECUTOR_FIELD = "invoker";
    private static final String THREADED_SELECTOR_WORKER_FIELD = "invoker";

    @Before
    public void setUp() {
        thriftDtpAdapter = new ThriftDtpAdapter();
        threadPoolExecutor = new ThreadPoolExecutor(
                5, 10, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100));
    }

    @After
    public void tearDown() {
        thriftDtpAdapter = null;
        threadPoolExecutor.shutdownNow();
        tThreadPoolServer = null;
        tHsHaServer = null;
        tThreadedSelectorServer = null;
    }

    @Test
    public void testEnhanceTThreadPoolServer() throws IOException, TTransportException {
        ReflectionUtil.setFieldValue(THREAD_POOL_SERVER_EXECUTOR_FIELD, tThreadPoolServer, threadPoolExecutor);
        TServerSocket tServerSocket = new TServerSocket(new ServerSocket(8989));
        ReflectionUtil.setFieldValue("serverTransport_", tThreadPoolServer, tServerSocket);
        thriftDtpAdapter.initializeTThreadPoolServer(tThreadPoolServer);
        
        Map<String, ExecutorWrapper> executors = thriftDtpAdapter.getExecutorWrappers();
        Assert.assertFalse(executors.isEmpty());
        
        Object enhancedExecutor = ReflectionUtil.getFieldValue(
                TThreadPoolServer.class, THREAD_POOL_SERVER_EXECUTOR_FIELD, tThreadPoolServer);
        Assert.assertTrue(enhancedExecutor instanceof ThreadPoolExecutorProxy);
    }

    @Test
    public void testEnhanceTHsHaServer() throws IOException, TTransportException {
        ReflectionUtil.setFieldValue(HSHASERVER_EXECUTOR_FIELD, tHsHaServer, threadPoolExecutor);
        TServerSocket tServerSocket = new TServerSocket(new ServerSocket(8990));
        ReflectionUtil.setFieldValue("serverTransport_", tHsHaServer, tServerSocket);
        thriftDtpAdapter.initializeTHsHaServer(tHsHaServer);
        
        Map<String, ExecutorWrapper> executors = thriftDtpAdapter.getExecutorWrappers();
        Assert.assertFalse(executors.isEmpty());
        
        Object enhancedExecutor = ReflectionUtil.getFieldValue(
                THsHaServer.class, HSHASERVER_EXECUTOR_FIELD, tHsHaServer);
        Assert.assertTrue(enhancedExecutor instanceof ThreadPoolExecutorProxy);
    }

    @Test
    public void testEnhanceTThreadedSelectorServer() throws IOException, TTransportException {
        ReflectionUtil.setFieldValue(THREADED_SELECTOR_WORKER_FIELD, tThreadedSelectorServer, threadPoolExecutor);
        TServerSocket tServerSocket = new TServerSocket(new ServerSocket(8991));
        ReflectionUtil.setFieldValue("serverTransport_", tThreadedSelectorServer, tServerSocket);
        thriftDtpAdapter.initializeTThreadedSelectorServer(tThreadedSelectorServer);
        
        Map<String, ExecutorWrapper> executors = thriftDtpAdapter.getExecutorWrappers();
        Assert.assertFalse(executors.isEmpty());
        
        Object enhancedExecutor = ReflectionUtil.getFieldValue(
                TThreadedSelectorServer.class, THREADED_SELECTOR_WORKER_FIELD, tThreadedSelectorServer);
        Assert.assertTrue(enhancedExecutor instanceof ThreadPoolExecutorProxy);
    }
}
