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

package org.dromara.dynamictp.test.core.notify.manager;

import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.pattern.filter.Invoker;
import org.dromara.dynamictp.common.pattern.filter.InvokerChain;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.adapter.ExecutorAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AlarmManager test
 *
 * @author yanhom
 * @since 1.2.2
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AlarmManagerTest.ContextConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
class AlarmManagerTest {

    private final AtomicReference<BaseNotifyCtx> alarmContext = new AtomicReference<>();

    private final AtomicInteger alarmCount = new AtomicInteger();

    private InvokerChain<BaseNotifyCtx> alarmInvokerChain;

    private Invoker<BaseNotifyCtx> originalHead;

    private Field headField;

    @BeforeEach
    void setUp() throws Exception {
        alarmInvokerChain = getAlarmInvokerChain();
        headField = InvokerChain.class.getDeclaredField("head");
        headField.setAccessible(true);
        originalHead = (Invoker<BaseNotifyCtx>) headField.get(alarmInvokerChain);
        Invoker<BaseNotifyCtx> testInvoker = context -> {
            alarmContext.set(context);
            alarmCount.incrementAndGet();
        };
        headField.set(alarmInvokerChain, testInvoker);
    }

    @AfterEach
    void tearDown() throws Exception {
        headField.set(alarmInvokerChain, originalHead);
    }

    @Test
    void testDoCheckAndTryAlarmTriggersWhenCapacityReachesThreshold() {
        NotifyItem notifyItem = notifyItem(NotifyItemEnum.CAPACITY, 80);
        ExecutorWrapper wrapper = executorWrapper("capacity-reached", notifyItem, mockExecutor(1, 1, 0, 8, 10));

        AlarmManager.doCheckAndTryAlarm(wrapper, NotifyItemEnum.CAPACITY);

        assertEquals(1, alarmCount.get());
        assertSame(notifyItem, alarmContext.get().getNotifyItem());
        assertEquals(NotifyItemEnum.CAPACITY, alarmContext.get().getNotifyItemEnum());
        assertEquals("capacity-reached", alarmContext.get().getExecutorWrapper().getThreadPoolName());
    }

    @Test
    void testDoCheckAndTryAlarmSkipsWhenCapacityBelowThreshold() {
        NotifyItem notifyItem = notifyItem(NotifyItemEnum.CAPACITY, 90);
        ExecutorWrapper wrapper = executorWrapper("capacity-below", notifyItem, mockExecutor(1, 1, 0, 8, 10));

        AlarmManager.doCheckAndTryAlarm(wrapper, NotifyItemEnum.CAPACITY);

        assertNoAlarm();
    }

    @Test
    void testDoCheckAndTryAlarmSkipsWhenQueueIsEmpty() {
        NotifyItem notifyItem = notifyItem(NotifyItemEnum.CAPACITY, 1);
        ExecutorWrapper wrapper = executorWrapper("capacity-empty", notifyItem, mockExecutor(1, 1, 0, 0, 10));

        AlarmManager.doCheckAndTryAlarm(wrapper, NotifyItemEnum.CAPACITY);

        assertNoAlarm();
    }

    @Test
    void testDoCheckAndTryAlarmTriggersWhenLivenessReachesThreshold() {
        NotifyItem notifyItem = notifyItem(NotifyItemEnum.LIVENESS, 70);
        ExecutorWrapper wrapper = executorWrapper("liveness-reached", notifyItem, mockExecutor(1, 10, 7, 0, 10));

        AlarmManager.doCheckAndTryAlarm(wrapper, NotifyItemEnum.LIVENESS);

        assertEquals(1, alarmCount.get());
        assertSame(notifyItem, alarmContext.get().getNotifyItem());
        assertEquals(NotifyItemEnum.LIVENESS, alarmContext.get().getNotifyItemEnum());
        assertEquals(7, alarmContext.get().getExecutorWrapper().getExecutor().getActiveCount());
    }

    @Test
    void testDoCheckAndTryAlarmSkipsWhenLivenessBelowThreshold() {
        NotifyItem notifyItem = notifyItem(NotifyItemEnum.LIVENESS, 80);
        ExecutorWrapper wrapper = executorWrapper("liveness-below", notifyItem, mockExecutor(1, 10, 7, 0, 10));

        AlarmManager.doCheckAndTryAlarm(wrapper, NotifyItemEnum.LIVENESS);

        assertNoAlarm();
    }

    @Test
    void testDoCheckAndTryAlarmTriggersDirectAlarmTypesWithoutThresholdCheck() {
        NotifyItem notifyItem = notifyItem(NotifyItemEnum.REJECT, 100);
        ExecutorWrapper wrapper = executorWrapper("reject-direct", notifyItem, mockExecutor(1, 10, 0, 0, 10));

        AlarmManager.doCheckAndTryAlarm(wrapper, NotifyItemEnum.REJECT);

        assertEquals(1, alarmCount.get());
        assertSame(notifyItem, alarmContext.get().getNotifyItem());
        assertEquals(NotifyItemEnum.REJECT, alarmContext.get().getNotifyItemEnum());
    }

    @Test
    void testDoCheckAndTryAlarmSkipsUnsupportedScheduleType() {
        NotifyItem notifyItem = notifyItem(NotifyItemEnum.CHANGE, 1);
        ExecutorWrapper wrapper = executorWrapper("change-unsupported", notifyItem, mockExecutor(1, 10, 10, 10, 10));

        AlarmManager.doCheckAndTryAlarm(wrapper, NotifyItemEnum.CHANGE);

        assertNoAlarm();
    }

    @Test
    void testDoTryAlarmTriggersWhenNotifyItemExists() {
        NotifyItem notifyItem = notifyItem(NotifyItemEnum.QUEUE_TIMEOUT, 100);
        ExecutorWrapper wrapper = executorWrapper("queue-timeout-direct", notifyItem, mockExecutor(1, 10, 0, 0, 10));

        AlarmManager.doTryAlarm(wrapper, NotifyItemEnum.QUEUE_TIMEOUT);

        assertEquals(1, alarmCount.get());
        assertSame(notifyItem, alarmContext.get().getNotifyItem());
        assertEquals(NotifyItemEnum.QUEUE_TIMEOUT, alarmContext.get().getNotifyItemEnum());
    }

    @Test
    void testDoTryAlarmSkipsWhenNotifyItemMissing() {
        NotifyItem notifyItem = notifyItem(NotifyItemEnum.RUN_TIMEOUT, 100);
        ExecutorWrapper wrapper = executorWrapper("missing-notify-item", notifyItem, mockExecutor(1, 10, 0, 0, 10));

        AlarmManager.doTryAlarm(wrapper, NotifyItemEnum.REJECT);

        assertNoAlarm();
    }

    private void assertNoAlarm() {
        assertEquals(0, alarmCount.get());
        assertNull(alarmContext.get());
    }

    private NotifyItem notifyItem(NotifyItemEnum notifyItemEnum, int threshold) {
        NotifyItem notifyItem = new NotifyItem();
        notifyItem.setType(notifyItemEnum.getValue());
        notifyItem.setThreshold(threshold);
        notifyItem.setPlatformIds(Collections.singletonList("platform"));
        return notifyItem;
    }

    private ExecutorWrapper executorWrapper(String poolName, NotifyItem notifyItem, ExecutorAdapter<?> executor) {
        ExecutorWrapper wrapper = new ExecutorWrapper(poolName, executor);
        wrapper.setNotifyItems(Collections.singletonList(notifyItem));
        wrapper.setNotifyEnabled(true);
        return wrapper;
    }

    private ExecutorAdapter<?> mockExecutor(int corePoolSize,
                                            int maximumPoolSize,
                                            int activeCount,
                                            int queueSize,
                                            int queueCapacity) {
        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(queueCapacity);
        for (int i = 0; i < queueSize; i++) {
            assertTrue(queue.offer(() -> {
                // mock queued task
            }));
        }
        return new TestExecutorAdapter(corePoolSize, maximumPoolSize, activeCount, queue);
    }

    private InvokerChain<BaseNotifyCtx> getAlarmInvokerChain() throws Exception {
        Field field = AlarmManager.class.getDeclaredField("ALARM_INVOKER_CHAIN");
        field.setAccessible(true);
        return (InvokerChain<BaseNotifyCtx>) field.get(null);
    }

    @Configuration
    static class ContextConfig {

        @Bean
        org.dromara.dynamictp.spring.holder.SpringContextHolder springContextHolder() {
            return new org.dromara.dynamictp.spring.holder.SpringContextHolder();
        }
    }

    private static class TestExecutorAdapter implements ExecutorAdapter<Executor> {

        private final int corePoolSize;

        private final int maximumPoolSize;

        private final int activeCount;

        private final LinkedBlockingQueue<Runnable> queue;

        TestExecutorAdapter(int corePoolSize,
                            int maximumPoolSize,
                            int activeCount,
                            LinkedBlockingQueue<Runnable> queue) {
            this.corePoolSize = corePoolSize;
            this.maximumPoolSize = maximumPoolSize;
            this.activeCount = activeCount;
            this.queue = queue;
        }

        @Override
        public Executor getOriginal() {
            return Runnable::run;
        }

        @Override
        public int getCorePoolSize() {
            return corePoolSize;
        }

        @Override
        public void setCorePoolSize(int corePoolSize) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getMaximumPoolSize() {
            return maximumPoolSize;
        }

        @Override
        public void setMaximumPoolSize(int maximumPoolSize) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getPoolSize() {
            return activeCount;
        }

        @Override
        public int getActiveCount() {
            return activeCount;
        }

        @Override
        public int getLargestPoolSize() {
            return maximumPoolSize;
        }

        @Override
        public long getTaskCount() {
            return activeCount + queue.size();
        }

        @Override
        public long getCompletedTaskCount() {
            return 0L;
        }

        @Override
        public LinkedBlockingQueue<Runnable> getQueue() {
            return queue;
        }

        @Override
        public String getQueueType() {
            return queue.getClass().getSimpleName();
        }

        @Override
        public int getQueueSize() {
            return queue.size();
        }

        @Override
        public int getQueueRemainingCapacity() {
            return queue.remainingCapacity();
        }

        @Override
        public int getQueueCapacity() {
            return queue.size() + queue.remainingCapacity();
        }

        @Override
        public String getRejectHandlerType() {
            return "AbortPolicy";
        }

        @Override
        public boolean allowsCoreThreadTimeOut() {
            return false;
        }

        @Override
        public long getKeepAliveTime(TimeUnit unit) {
            return unit.convert(60, TimeUnit.SECONDS);
        }
    }
}
