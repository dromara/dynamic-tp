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

import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.dromara.dynamictp.adapter.thrift.ThriftDtpAdapter;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.proxy.ThreadPoolExecutorProxy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;
import java.util.concurrent.ExecutorService;
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

    @Test
    public void testEnhanceTThreadPoolServer() {
        ReflectionUtil.setFieldValue(THREAD_POOL_SERVER_EXECUTOR_FIELD, tThreadPoolServer, threadPoolExecutor);
        
        thriftDtpAdapter.initializeTThreadPoolServer(tThreadPoolServer);
        
        Map<String, ExecutorWrapper> executors = thriftDtpAdapter.getExecutorWrappers();
        Assert.assertFalse(executors.isEmpty());
        
        Object enhancedExecutor = ReflectionUtil.getFieldValue(
                TThreadPoolServer.class, THREAD_POOL_SERVER_EXECUTOR_FIELD, tThreadPoolServer);
        Assert.assertTrue(enhancedExecutor instanceof ThreadPoolExecutorProxy);
    }

    @Test
    public void testEnhanceTHsHaServer() {
        ReflectionUtil.setFieldValue(HSHASERVER_EXECUTOR_FIELD, tHsHaServer, threadPoolExecutor);
        
        thriftDtpAdapter.initializeTHsHaServer(tHsHaServer);
        
        Map<String, ExecutorWrapper> executors = thriftDtpAdapter.getExecutorWrappers();
        Assert.assertFalse(executors.isEmpty());
        
        Object enhancedExecutor = ReflectionUtil.getFieldValue(
                THsHaServer.class, HSHASERVER_EXECUTOR_FIELD, tHsHaServer);
        Assert.assertTrue(enhancedExecutor instanceof ThreadPoolExecutorProxy);
    }

    @Test
    public void testEnhanceTThreadedSelectorServer() {
        ReflectionUtil.setFieldValue(THREADED_SELECTOR_WORKER_FIELD, tThreadedSelectorServer, threadPoolExecutor);
        
        thriftDtpAdapter.initializeTThreadedSelectorServer(tThreadedSelectorServer);
        
        Map<String, ExecutorWrapper> executors = thriftDtpAdapter.getExecutorWrappers();
        Assert.assertFalse(executors.isEmpty());
        
        Object enhancedExecutor = ReflectionUtil.getFieldValue(
                TThreadedSelectorServer.class, THREADED_SELECTOR_WORKER_FIELD, tThreadedSelectorServer);
        Assert.assertTrue(enhancedExecutor instanceof ThreadPoolExecutorProxy);
    }
    
    @Test
    public void testGetServerPort() {
        Object mockServerTransport = Mockito.mock(Object.class);
        Object mockServerSocket = Mockito.mock(Object.class);
        
        ReflectionUtil.setFieldValue("serverTransport_", tThreadPoolServer, mockServerTransport);
        ReflectionUtil.setFieldValue("serverSocket_", mockServerTransport, mockServerSocket);
        
        try {
            java.lang.reflect.Method getServerPortMethod = ThriftDtpAdapter.class.getDeclaredMethod("getServerPort", Object.class);
            getServerPortMethod.setAccessible(true);
            
            java.lang.reflect.Method mockLocalPortMethod = Mockito.mock(java.lang.reflect.Method.class);
            Mockito.when(mockLocalPortMethod.invoke(mockServerSocket)).thenReturn(9090);
            
            Mockito.mockStatic(ReflectionUtil.class);
            Mockito.when(ReflectionUtil.getFieldValue("serverTransport_", tThreadPoolServer)).thenReturn(mockServerTransport);
            Mockito.when(ReflectionUtil.getFieldValue("serverSocket_", mockServerTransport)).thenReturn(mockServerSocket);
            Mockito.when(ReflectionUtil.findMethod(mockServerSocket.getClass(), "getLocalPort")).thenReturn(mockLocalPortMethod);
            
            int port = (int) getServerPortMethod.invoke(thriftDtpAdapter, tThreadPoolServer);
            Assert.assertEquals(9090, port);
        } catch (Exception e) {
            Assert.fail("Test failed with exception: " + e.getMessage());
        }
    }
}
