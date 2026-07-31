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

package org.dromara.dynamictp.spring.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Adapts Spring's {@link SimpleAsyncTaskExecutor} (used by Spring Boot 3 when
 * {@code spring.threads.virtual.enabled=true}) to the standard
 * {@link java.util.concurrent.ExecutorService} contract.
 *
 * <p>{@code SimpleAsyncTaskExecutor} is {@link AutoCloseable} but not
 * {@code ExecutorService}: it only exposes {@code execute} / {@code submit}
 * and a {@code close()} method. This adapter fills the lifecycle gap
 * (shutdown / isShutdown / awaitTermination) so it can be wrapped uniformly
 * by {@link org.dromara.dynamictp.core.support.proxy.VirtualThreadExecutorProxy}.
 * The {@code submit} / {@code invokeAll} / {@code invokeAny} boilerplate is
 * provided by {@link AbstractExecutorService}.</p>
 *
 * <p><b>Ownership</b>: the adapted executor is a Spring bean that dtp only observes,
 * it does not own it. {@code SimpleAsyncTaskExecutor} implements {@link AutoCloseable},
 * so the container closes it on shutdown. Therefore {@link #shutdown()} only flips the
 * local state and never closes the delegate: dtp's own lifecycle runs at
 * {@code Integer.MAX_VALUE} phase (i.e. first on shutdown), and closing the application
 * task executor there would make every async task submitted during the remaining
 * shutdown sequence fail.</p>
 *
 * @author yanhom
 * @since 1.3.0
 */
@Slf4j
public class SimpleAsyncTaskExecutorAdapter extends AbstractExecutorService {

    private final SimpleAsyncTaskExecutor delegate;

    private volatile boolean shutdown = false;

    public SimpleAsyncTaskExecutorAdapter(SimpleAsyncTaskExecutor delegate) {
        this.delegate = delegate;
    }

    public SimpleAsyncTaskExecutor getDelegate() {
        return delegate;
    }

    @Override
    public void execute(Runnable command) {
        if (shutdown) {
            // ExecutorService contract: refusing a task is signalled by RejectedExecutionException,
            // which also lets the aware chain treat it as a rejection.
            throw new RejectedExecutionException("Executor has been shut down: " + delegate);
        }
        delegate.execute(command);
    }

    @Override
    public void shutdown() {
        shutdown = true;
        log.debug("DynamicTp shutdown SimpleAsyncTaskExecutor adapter, the adapted executor is owned by "
                + "the Spring container and is left untouched: {}", delegate);
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return shutdown;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        // SimpleAsyncTaskExecutor has no in-flight tracking we can await on;
        // once shut down, treat it as terminated.
        return shutdown;
    }
}
