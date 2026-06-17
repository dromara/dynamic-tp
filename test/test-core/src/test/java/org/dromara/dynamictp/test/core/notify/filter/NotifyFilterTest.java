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

package org.dromara.dynamictp.test.core.notify.filter;

import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.pattern.filter.Invoker;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.notifier.alarm.AlarmCounter;
import org.dromara.dynamictp.core.notifier.alarm.AlarmLimiter;
import org.dromara.dynamictp.core.notifier.chain.filter.BaseAlarmFilter;
import org.dromara.dynamictp.core.notifier.chain.filter.BaseNoticeFilter;
import org.dromara.dynamictp.core.notifier.chain.filter.SilentCheckFilter;
import org.dromara.dynamictp.core.notifier.context.AlarmCtx;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * NotifyFilter test.
 *
 * @author yanhom
 * @since 1.2.2
 */
class NotifyFilterTest {

    @Test
    void testBaseNoticeFilterInvokesNextWhenNotifyEnabled() {
        BaseNoticeFilter filter = new BaseNoticeFilter();
        BaseNotifyCtx context = context("notice-enabled", notifyItem(NotifyItemEnum.CHANGE, 1, 0), true);
        AtomicInteger invokeCount = new AtomicInteger();

        filter.doFilter(context, countingInvoker(invokeCount));

        assertEquals(1, invokeCount.get());
    }

    @Test
    void testBaseNoticeFilterStopsWhenNotifyItemDisabled() {
        BaseNoticeFilter filter = new BaseNoticeFilter();
        NotifyItem notifyItem = notifyItem(NotifyItemEnum.CHANGE, 1, 0);
        notifyItem.setEnabled(false);
        BaseNotifyCtx context = context("notice-disabled-item", notifyItem, true);
        AtomicInteger invokeCount = new AtomicInteger();

        filter.doFilter(context, countingInvoker(invokeCount));

        assertEquals(0, invokeCount.get());
    }

    @Test
    void testBaseNoticeFilterStopsWhenWrapperDisabled() {
        BaseNoticeFilter filter = new BaseNoticeFilter();
        BaseNotifyCtx context = context("notice-disabled-wrapper", notifyItem(NotifyItemEnum.CHANGE, 1, 0), false);
        AtomicInteger invokeCount = new AtomicInteger();

        filter.doFilter(context, countingInvoker(invokeCount));

        assertEquals(0, invokeCount.get());
    }

    @Test
    void testBaseAlarmFilterWaitsUntilAlarmCountReachesThreshold() {
        BaseAlarmFilter filter = new BaseAlarmFilter();
        NotifyItem notifyItem = notifyItem(NotifyItemEnum.REJECT, 2, 0);
        String poolName = "alarm-threshold";
        AlarmCounter.initAlarmCounter(poolName, notifyItem);
        AlarmCtx context = alarmContext(poolName, notifyItem, true);
        AtomicInteger invokeCount = new AtomicInteger();

        filter.doFilter(context, countingInvoker(invokeCount));
        assertEquals(0, invokeCount.get());

        filter.doFilter(context, countingInvoker(invokeCount));

        assertEquals(1, invokeCount.get());
        assertNotNull(context.getAlarmInfo());
        assertEquals(2, context.getAlarmInfo().getCount());
    }

    @Test
    void testBaseAlarmFilterStopsWhenPlatformsMissing() {
        BaseAlarmFilter filter = new BaseAlarmFilter();
        NotifyItem notifyItem = notifyItem(NotifyItemEnum.REJECT, 1, 0);
        notifyItem.setPlatformIds(Collections.emptyList());
        String poolName = "alarm-no-platform";
        AlarmCounter.initAlarmCounter(poolName, notifyItem);
        AlarmCtx context = alarmContext(poolName, notifyItem, true);
        AtomicInteger invokeCount = new AtomicInteger();

        filter.doFilter(context, countingInvoker(invokeCount));

        assertEquals(0, invokeCount.get());
    }

    @Test
    void testSilentCheckFilterInvokesOnceWithinSilencePeriod() {
        SilentCheckFilter filter = new SilentCheckFilter();
        NotifyItem notifyItem = notifyItem(NotifyItemEnum.REJECT, 1, 60);
        String poolName = "silent-check";
        AlarmLimiter.initAlarmLimiter(poolName, notifyItem);
        BaseNotifyCtx context = context(poolName, notifyItem, true);
        AtomicInteger invokeCount = new AtomicInteger();

        filter.doFilter(context, countingInvoker(invokeCount));
        filter.doFilter(context, countingInvoker(invokeCount));

        assertEquals(1, invokeCount.get());
    }

    @Test
    void testSilentCheckFilterBypassesWhenSilencePeriodDisabled() {
        SilentCheckFilter filter = new SilentCheckFilter();
        BaseNotifyCtx context = context("silent-disabled", notifyItem(NotifyItemEnum.REJECT, 1, 0), true);
        AtomicInteger invokeCount = new AtomicInteger();

        filter.doFilter(context, countingInvoker(invokeCount));
        filter.doFilter(context, countingInvoker(invokeCount));

        assertEquals(2, invokeCount.get());
    }

    private BaseNotifyCtx context(String poolName, NotifyItem notifyItem, boolean notifyEnabled) {
        BaseNotifyCtx context = new BaseNotifyCtx();
        context.setExecutorWrapper(executorWrapper(poolName, notifyEnabled));
        context.setNotifyItem(notifyItem);
        return context;
    }

    private AlarmCtx alarmContext(String poolName, NotifyItem notifyItem, boolean notifyEnabled) {
        return new AlarmCtx(executorWrapper(poolName, notifyEnabled), notifyItem);
    }

    private ExecutorWrapper executorWrapper(String poolName, boolean notifyEnabled) {
        DtpExecutor executor = new DtpExecutor(1, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        executor.setThreadPoolName(poolName);
        executor.setNotifyEnabled(notifyEnabled);
        ExecutorWrapper wrapper = new ExecutorWrapper(executor);
        wrapper.setNotifyEnabled(notifyEnabled);
        return wrapper;
    }

    private NotifyItem notifyItem(NotifyItemEnum notifyItemEnum, int count, int silencePeriod) {
        NotifyItem notifyItem = new NotifyItem();
        notifyItem.setType(notifyItemEnum.getValue());
        notifyItem.setCount(count);
        notifyItem.setPeriod(120);
        notifyItem.setSilencePeriod(silencePeriod);
        notifyItem.setPlatformIds(Collections.singletonList("platform"));
        return notifyItem;
    }

    private Invoker<BaseNotifyCtx> countingInvoker(AtomicInteger invokeCount) {
        return context -> invokeCount.incrementAndGet();
    }
}
