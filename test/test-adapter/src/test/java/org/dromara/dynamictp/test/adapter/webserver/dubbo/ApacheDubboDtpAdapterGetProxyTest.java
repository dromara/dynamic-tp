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

package org.dromara.dynamictp.test.adapter.webserver.dubbo;

import org.dromara.dynamictp.adapter.dubbo.apache.ApacheDubboDtpAdapter;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ApacheDubboDtpAdapter#getProxy 对非 ThreadPoolExecutor 执行器（如 Dubbo 3.x 虚拟线程场景下的
 * ThreadPerTaskExecutor）应优雅跳过，而不是抛 ClassCastException 导致整个 dubboTp 初始化失败（issue #583）。
 */
public class ApacheDubboDtpAdapterGetProxyTest {

    private final ApacheDubboDtpAdapter adapter = new ApacheDubboDtpAdapter();

    private ThreadPoolExecutor invokeGetProxy(Executor executor) throws Exception {
        Method getProxy = ApacheDubboDtpAdapter.class.getDeclaredMethod("getProxy", Executor.class);
        getProxy.setAccessible(true);
        return (ThreadPoolExecutor) getProxy.invoke(adapter, executor);
    }

    @Test
    public void getProxy_shouldReturnNullForNonThreadPoolExecutor() throws Exception {
        // ForkJoinPool 不是 ThreadPoolExecutor，模拟 ThreadPerTaskExecutor 这类无法增强的执行器类型
        Executor unsupported = Executors.newWorkStealingPool();
        Assertions.assertNull(invokeGetProxy(unsupported));
    }

    @Test
    public void getProxy_shouldWrapThreadPoolExecutor() throws Exception {
        Executor executor = Executors.newFixedThreadPool(1);
        ThreadPoolExecutor proxy = invokeGetProxy(executor);
        Assertions.assertNotNull(proxy);
        Assertions.assertNotSame(executor, proxy);
    }

}
