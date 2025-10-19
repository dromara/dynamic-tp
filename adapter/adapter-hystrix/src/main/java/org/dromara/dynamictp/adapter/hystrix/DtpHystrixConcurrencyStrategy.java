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
import org.dromara.dynamictp.core.support.proxy.ThreadPoolExecutorProxy;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * DtpHystrixConcurrencyStrategy related
 *
 * @author devin
 * @since 1.2.2
 */
@Slf4j
public class DtpHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {

    private final HystrixConcurrencyStrategy delegate;
    private final HystrixDtpAdapter hystrixDtpAdapter;

    public DtpHystrixConcurrencyStrategy(HystrixConcurrencyStrategy delegate, HystrixDtpAdapter hystrixDtpAdapter) {
        this.delegate = delegate;
        this.hystrixDtpAdapter = hystrixDtpAdapter;
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
        hystrixDtpAdapter.registerExecutor(threadPoolKey.name(), proxy, originalExecutor);
        return proxy;
    }

    @Override
    public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey, HystrixThreadPoolProperties threadPoolProperties) {
        ThreadPoolExecutor originalExecutor = delegate.getThreadPool(threadPoolKey, threadPoolProperties);
        ThreadPoolExecutorProxy proxy = new ThreadPoolExecutorProxy(originalExecutor);
        hystrixDtpAdapter.registerExecutor(threadPoolKey.name(), proxy, originalExecutor);
        return proxy;
    }

    @Override
    public BlockingQueue<Runnable> getBlockingQueue(int maxQueueSize) {
        return delegate.getBlockingQueue(maxQueueSize);
    }

    @Override
    public <T> Callable<T> wrapCallable(Callable<T> callable) {
        return delegate.wrapCallable(callable);
    }
}
