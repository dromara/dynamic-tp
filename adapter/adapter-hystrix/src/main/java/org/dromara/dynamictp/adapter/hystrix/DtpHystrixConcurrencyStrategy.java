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

package org.dromara.dynamictp.adapter.hystrix;

import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.properties.HystrixProperty;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.manager.ContextManagerHelper;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.proxy.ThreadPoolExecutorProxy;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * DtpHystrixConcurrencyStrategy related
 *
 * @author yanhom
 * @since 1.0.8
 */
@Slf4j
public class DtpHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {

    private final HystrixConcurrencyStrategy delegate;
    private final HystrixDtpAdapter hystrixDtpAdapter;

    public DtpHystrixConcurrencyStrategy(HystrixConcurrencyStrategy delegate) {
        this.delegate = delegate;
        this.hystrixDtpAdapter = ContextManagerHelper.getBean(HystrixDtpAdapter.class);
    }

    @Override
    public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey,
                                           HystrixProperty<Integer> corePoolSize,
                                           HystrixProperty<Integer> maximumPoolSize,
                                           HystrixProperty<Integer> keepAliveTime,
                                           TimeUnit unit,
                                           BlockingQueue<Runnable> workQueue) {
        ThreadPoolExecutor originalExecutor = delegate.getThreadPool(
                threadPoolKey, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        
        ThreadPoolExecutorProxy proxy = new ThreadPoolExecutorProxy(originalExecutor);
        
        String poolName = HystrixDtpAdapter.TP_PREFIX + "#" + threadPoolKey.name();
        ExecutorWrapper wrapper = new ExecutorWrapper(poolName, proxy);
        hystrixDtpAdapter.registerExecutor(poolName, wrapper);
        
        log.info("DynamicTp adapter, created enhanced thread pool for Hystrix: {}", poolName);
        
        return proxy;
    }

    @Override
    public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey, HystrixThreadPoolProperties threadPoolProperties) {
        ThreadPoolExecutor originalExecutor = delegate.getThreadPool(threadPoolKey, threadPoolProperties);
        
        ThreadPoolExecutorProxy proxy = new ThreadPoolExecutorProxy(originalExecutor);
        
        String poolName = HystrixDtpAdapter.TP_PREFIX + "#" + threadPoolKey.name();
        ExecutorWrapper wrapper = new ExecutorWrapper(poolName, proxy);
        hystrixDtpAdapter.registerExecutor(poolName, wrapper);
        
        log.info("DynamicTp adapter, created enhanced thread pool for Hystrix: {}", poolName);
        
        return proxy;
    }

    @Override
    public BlockingQueue<Runnable> getBlockingQueue(int maxQueueSize) {
        return delegate.getBlockingQueue(maxQueueSize);
    }

    @Override
    public <T> java.util.concurrent.Callable<T> wrapCallable(java.util.concurrent.Callable<T> callable) {
        return delegate.wrapCallable(callable);
    }
}
