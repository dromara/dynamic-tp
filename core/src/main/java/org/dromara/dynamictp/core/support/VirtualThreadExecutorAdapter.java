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

package org.dromara.dynamictp.core.support;

import org.dromara.dynamictp.core.support.adapter.ExecutorAdapter;
import org.dromara.dynamictp.core.support.proxy.VirtualThreadExecutorProxy;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;

/**
 * {@link ExecutorAdapter} view over a {@link VirtualThreadExecutorProxy}.
 *
 * <p>Unlike the old prototype (which unwrapped the proxy and stored the bare
 * delegate, silently dropping task-wrappers / aware / notify state), this adapter
 * keeps a reference to the proxy itself. {@code getOriginal()} returns the proxy,
 * so {@code ExecutorWrapper#setTaskWrappers} / {@code setRejectHandler} keep working
 * via the {@code TaskEnhanceAware} / {@code RejectHandlerAware} contracts.</p>
 *
 * <p>Virtual threads are unbounded and have no queue, so size / queue / keepAlive
 * metrics return {@code -1} (the {@code unsupported} convention already used by
 * {@link ExecutorAdapter}'s default methods).</p>
 *
 * @author yanhom
 * @since 1.x.x
 */
public class VirtualThreadExecutorAdapter implements ExecutorAdapter<VirtualThreadExecutorProxy> {

    private final VirtualThreadExecutorProxy proxy;

    public VirtualThreadExecutorAdapter(VirtualThreadExecutorProxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public VirtualThreadExecutorProxy getOriginal() {
        return proxy;
    }

    @Override
    public void execute(Runnable command) {
        proxy.execute(command);
    }

    @Override
    public int getCorePoolSize() {
        return -1;
    }

    @Override
    public void setCorePoolSize(int corePoolSize) {
        // unsupported: virtual threads are unbounded
    }

    @Override
    public int getMaximumPoolSize() {
        return -1;
    }

    @Override
    public void setMaximumPoolSize(int maximumPoolSize) {
        // unsupported: virtual threads are unbounded
    }

    @Override
    public int getPoolSize() {
        return -1;
    }

    @Override
    public int getActiveCount() {
        return -1;
    }

    @Override
    public boolean isShutdown() {
        return proxy.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return proxy.isTerminated();
    }

    @Override
    public String getRejectHandlerType() {
        return proxy.getRejectHandlerType();
    }

    @Override
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        // unsupported: virtual threads never reject (unbounded), but keep the
        // reject handler type so notify / refresh can still report it.
        if (handler != null) {
            proxy.setRejectHandlerType(handler.getClass().getSimpleName());
        }
    }

    @Override
    public long getKeepAliveTime(TimeUnit unit) {
        return -1;
    }

    @Override
    public void setKeepAliveTime(long time, TimeUnit unit) {
        // unsupported
    }

    @Override
    public boolean allowsCoreThreadTimeOut() {
        return false;
    }

    @Override
    public void allowCoreThreadTimeOut(boolean value) {
        // unsupported
    }
}
