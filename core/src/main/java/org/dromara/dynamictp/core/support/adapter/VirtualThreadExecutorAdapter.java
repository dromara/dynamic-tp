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
package org.dromara.dynamictp.core.support.adapter;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * ClassName: VirtualThreadExecutorAdapter
 * Package: org.dromara.dynamictp.core.support
 * Description:
 * Adapter for virtual thread executor
 *
 * @author CYC
 */
public class VirtualThreadExecutorAdapter implements ExecutorAdapter<ExecutorService> {

    private final ExecutorService executor;

    public VirtualThreadExecutorAdapter(Executor executor) {
        this.executor = (ExecutorService) executor;
    }

    @Override
    public ExecutorService getOriginal() {
        return this.executor;
    }

    @Override
    public void execute(Runnable command) {
        this.executor.execute(command);
    }

    @Override
    public int getCorePoolSize() {
        return 0;
    }

    @Override
    public void setCorePoolSize(int corePoolSize) {

    }

    @Override
    public int getMaximumPoolSize() {
        return 0;
    }

    @Override
    public void setMaximumPoolSize(int maximumPoolSize) {

    }

    @Override
    public int getPoolSize() {
        return 0;
    }

    @Override
    public int getActiveCount() {
        return 0;
    }

    @Override
    public boolean isShutdown() {
        return this.executor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return this.executor.isTerminated();
    }


}
