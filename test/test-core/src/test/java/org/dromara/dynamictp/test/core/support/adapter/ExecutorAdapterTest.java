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

package org.dromara.dynamictp.test.core.support.adapter;

import org.dromara.dynamictp.core.support.adapter.ExecutorAdapter;
import org.dromara.dynamictp.core.support.adapter.ThreadPoolExecutorAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ExecutorAdapterTest related.
 */
class ExecutorAdapterTest {

    private ThreadPoolExecutor executor;

    @AfterEach
    void tearDown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    void testDefaultAdapterMethodsReturnUnsupportedValues() {
        ExecutorAdapter<Executor> adapter = new MinimalExecutorAdapter();

        Assertions.assertEquals(-1, adapter.getLargestPoolSize());
        Assertions.assertEquals(-1, adapter.getTaskCount());
        Assertions.assertEquals(-1, adapter.getCompletedTaskCount());
        Assertions.assertEquals("UnsupportedBlockingQueue", adapter.getQueueType());
        Assertions.assertEquals(0, adapter.getQueueSize());
        Assertions.assertEquals(0, adapter.getQueueRemainingCapacity());
        Assertions.assertEquals(0, adapter.getQueueCapacity());
        Assertions.assertNull(adapter.getRejectedExecutionHandler());
        Assertions.assertEquals("unknown", adapter.getRejectHandlerType());
        Assertions.assertFalse(adapter.allowsCoreThreadTimeOut());
        Assertions.assertEquals(-1, adapter.getKeepAliveTime(TimeUnit.MILLISECONDS));
        Assertions.assertFalse(adapter.isShutdown());
        Assertions.assertFalse(adapter.isTerminated());
        Assertions.assertFalse(adapter.isTerminating());
    }

    @Test
    void testUnsupportedBlockingQueueOperationsThrow() {
        ExecutorAdapter.UnsupportedBlockingQueue queue = new ExecutorAdapter.UnsupportedBlockingQueue();

        Assertions.assertThrows(UnsupportedOperationException.class, queue::iterator);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> queue.offer(() -> { }));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> queue.offer(() -> { }, 1, TimeUnit.MILLISECONDS));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> queue.put(() -> { }));
        Assertions.assertThrows(UnsupportedOperationException.class, queue::take);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> queue.poll(1, TimeUnit.MILLISECONDS));
        Assertions.assertThrows(UnsupportedOperationException.class, queue::poll);
        Assertions.assertThrows(UnsupportedOperationException.class, queue::peek);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> queue.drainTo(new ArrayList<>()));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> queue.drainTo(new ArrayList<>(), 1));
    }

    @Test
    void testThreadPoolExecutorAdapterDelegatesConfiguration() {
        executor = new ThreadPoolExecutor(1, 2, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        ThreadPoolExecutorAdapter adapter = new ThreadPoolExecutorAdapter(executor);

        adapter.setCorePoolSize(2);
        adapter.setMaximumPoolSize(3);
        adapter.setKeepAliveTime(5, TimeUnit.SECONDS);
        adapter.allowCoreThreadTimeOut(true);

        Assertions.assertSame(executor, adapter.getOriginal());
        Assertions.assertEquals(2, adapter.getCorePoolSize());
        Assertions.assertEquals(3, adapter.getMaximumPoolSize());
        Assertions.assertEquals(5, adapter.getKeepAliveTime(TimeUnit.SECONDS));
        Assertions.assertTrue(adapter.allowsCoreThreadTimeOut());
        Assertions.assertEquals(executor.getQueue(), adapter.getQueue());
    }

    @Test
    void testThreadPoolExecutorAdapterDelegatesExecuteAndShutdownState() throws InterruptedException {
        executor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        ThreadPoolExecutorAdapter adapter = new ThreadPoolExecutorAdapter(executor);
        CountDownLatch latch = new CountDownLatch(1);

        adapter.execute(latch::countDown);

        Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));
        executor.shutdown();
        Assertions.assertTrue(adapter.isShutdown());
    }

    @Test
    void testThreadPoolExecutorAdapterDelegatesRuntimeStats() throws InterruptedException {
        executor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        ThreadPoolExecutorAdapter adapter = new ThreadPoolExecutorAdapter(executor);
        CountDownLatch running = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        adapter.execute(() -> {
            running.countDown();
            try {
                release.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Assertions.assertTrue(running.await(3, TimeUnit.SECONDS));
        Assertions.assertEquals(1, adapter.getPoolSize());
        Assertions.assertEquals(1, adapter.getActiveCount());
        Assertions.assertEquals(1, adapter.getLargestPoolSize());
        Assertions.assertEquals(1, adapter.getTaskCount());

        release.countDown();
        executor.shutdown();
        Assertions.assertTrue(executor.awaitTermination(3, TimeUnit.SECONDS));
        Assertions.assertEquals(1, adapter.getCompletedTaskCount());
        Assertions.assertTrue(adapter.isTerminated());
        Assertions.assertFalse(adapter.isTerminating());
    }

    @Test
    void testThreadPoolExecutorAdapterPreStartsCoreThreads() {
        executor = new ThreadPoolExecutor(2, 2, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        ThreadPoolExecutorAdapter adapter = new ThreadPoolExecutorAdapter(executor);

        adapter.preStartAllCoreThreads();

        Assertions.assertEquals(2, adapter.getPoolSize());
    }

    @Test
    void testThreadPoolExecutorAdapterDelegatesRejectedHandler() {
        executor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        ThreadPoolExecutorAdapter adapter = new ThreadPoolExecutorAdapter(executor);
        ThreadPoolExecutor.CallerRunsPolicy handler = new ThreadPoolExecutor.CallerRunsPolicy();

        adapter.setRejectedExecutionHandler(handler);

        Assertions.assertSame(handler, adapter.getRejectedExecutionHandler());
        Assertions.assertEquals("CallerRunsPolicy", adapter.getRejectHandlerType());
    }

    @Test
    void testThreadPoolExecutorAdapterQueueMetricsUseBackingQueue() {
        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(2);
        executor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, queue);
        ThreadPoolExecutorAdapter adapter = new ThreadPoolExecutorAdapter(executor);
        List<Runnable> drained = new ArrayList<>();

        queue.offer(() -> { });

        Assertions.assertEquals("LinkedBlockingQueue", adapter.getQueueType());
        Assertions.assertEquals(1, adapter.getQueueSize());
        Assertions.assertEquals(1, adapter.getQueueRemainingCapacity());
        Assertions.assertEquals(2, adapter.getQueueCapacity());
        Assertions.assertEquals(1, adapter.getQueue().drainTo(drained));
        Assertions.assertEquals(1, drained.size());
    }

    private static class MinimalExecutorAdapter implements ExecutorAdapter<Executor> {

        private final Executor executor = Runnable::run;

        @Override
        public Executor getOriginal() {
            return executor;
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
    }
}
