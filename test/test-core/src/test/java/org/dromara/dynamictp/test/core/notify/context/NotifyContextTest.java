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

package org.dromara.dynamictp.test.core.notify.context;

import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.AlarmInfo;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.entity.TpMainFields;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.notifier.capture.CapturedExecutor;
import org.dromara.dynamictp.core.notifier.context.AlarmCtx;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;
import org.dromara.dynamictp.core.notifier.context.DtpNotifyCtxHolder;
import org.dromara.dynamictp.core.notifier.context.NoticeCtx;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * NotifyContext test.
 */
class NotifyContextTest {

    private DtpExecutor executor;

    @AfterEach
    void tearDown() {
        DtpNotifyCtxHolder.remove();
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    void testBaseNotifyCtxCapturesExecutorWrapperAndResolvesNotifyItemEnum() {
        ExecutorWrapper wrapper = executorWrapper("base-context");
        NotifyItem notifyItem = notifyItem(NotifyItemEnum.REJECT);

        BaseNotifyCtx context = new BaseNotifyCtx(wrapper, notifyItem);

        assertEquals("base-context", context.getExecutorWrapper().getThreadPoolName());
        assertInstanceOf(CapturedExecutor.class, context.getExecutorWrapper().getExecutor());
        assertSame(notifyItem, context.getNotifyItem());
        assertEquals(NotifyItemEnum.REJECT, context.getNotifyItemEnum());
    }

    @Test
    void testAlarmCtxStoresAlarmInfo() {
        ExecutorWrapper wrapper = executorWrapper("alarm-context");
        NotifyItem notifyItem = notifyItem(NotifyItemEnum.CAPACITY);
        AlarmInfo alarmInfo = new AlarmInfo();

        AlarmCtx context = new AlarmCtx(wrapper, notifyItem);
        context.setAlarmInfo(alarmInfo);

        assertSame(alarmInfo, context.getAlarmInfo());
        assertEquals(NotifyItemEnum.CAPACITY, context.getNotifyItemEnum());
    }

    @Test
    void testNoticeCtxStoresOldFieldsAndDiffs() {
        ExecutorWrapper wrapper = executorWrapper("notice-context");
        NotifyItem notifyItem = notifyItem(NotifyItemEnum.CHANGE);
        TpMainFields oldFields = new TpMainFields();
        oldFields.setCorePoolSize(1);

        NoticeCtx context = new NoticeCtx(wrapper, notifyItem, oldFields,
                Arrays.asList("corePoolSize", "maximumPoolSize"));

        assertSame(oldFields, context.getOldFields());
        assertEquals(Arrays.asList("corePoolSize", "maximumPoolSize"), context.getDiffs());
        assertEquals(NotifyItemEnum.CHANGE, context.getNotifyItemEnum());
    }

    @Test
    void testDtpNotifyCtxHolderStoresAndRemovesThreadLocalContext() {
        BaseNotifyCtx context = new BaseNotifyCtx();

        DtpNotifyCtxHolder.set(context);

        assertSame(context, DtpNotifyCtxHolder.get());

        DtpNotifyCtxHolder.remove();

        assertNull(DtpNotifyCtxHolder.get());
    }

    private ExecutorWrapper executorWrapper(String poolName) {
        executor = new DtpExecutor(1, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        executor.setThreadPoolName(poolName);
        return new ExecutorWrapper(executor);
    }

    private NotifyItem notifyItem(NotifyItemEnum notifyItemEnum) {
        NotifyItem notifyItem = new NotifyItem();
        notifyItem.setType(notifyItemEnum.getValue());
        return notifyItem;
    }
}
