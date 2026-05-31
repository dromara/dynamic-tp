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

package org.dromara.dynamictp.test.core.notify;

import com.google.common.collect.Lists;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.AlarmInfo;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.entity.ServiceInstance;
import org.dromara.dynamictp.common.entity.TpMainFields;
import org.dromara.dynamictp.common.notifier.LarkNotifier;
import org.dromara.dynamictp.common.notifier.Notifier;
import org.dromara.dynamictp.common.util.CommonUtil;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.notifier.AbstractDtpNotifier;
import org.dromara.dynamictp.core.notifier.DtpDingNotifier;
import org.dromara.dynamictp.core.notifier.DtpLarkNotifier;
import org.dromara.dynamictp.core.notifier.DtpNotifier;
import org.dromara.dynamictp.core.notifier.context.AlarmCtx;
import org.dromara.dynamictp.core.notifier.context.DtpNotifyCtxHolder;
import org.dromara.dynamictp.core.notifier.context.NoticeCtx;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolCreator;
import org.dromara.dynamictp.spring.holder.SpringContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.List;

import static org.dromara.dynamictp.common.em.QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AbstractDtpNotifierTest related
 *
 * @author ruoan
 * @since 1.1.3
 */
class AbstractDtpNotifierTest {

    private final Notifier notifier = Mockito.mock(Notifier.class);

    private final DtpExecutor dtpExecutor = ThreadPoolCreator.createDynamicFast("test");

    private MockedStatic<SpringContextHolder> mockedSpringContextHolder;

    private MockedStatic<CommonUtil> mockedCommonUtil;

    @BeforeEach
    void setUp() {
        // SpringContextHolder MUST be mocked first: CommonUtil.<clinit> calls
        // ContextManagerHelper.getEnvironmentProperty() -> SpringContextHolder.getInstance()
        ApplicationContext contextMock = mock(ApplicationContext.class);
        Environment envMock = mock(Environment.class);
        when(contextMock.getEnvironment()).thenReturn(envMock);

        mockedSpringContextHolder = Mockito.mockStatic(SpringContextHolder.class);
        mockedSpringContextHolder.when(SpringContextHolder::getInstance).thenReturn(contextMock);

        // Now CommonUtil can initialize: its <clinit> will find a valid Spring context
        ServiceInstance serviceInstance = new ServiceInstance("localhost", 8080, "test", "dev");
        mockedCommonUtil = Mockito.mockStatic(CommonUtil.class);
        mockedCommonUtil.when(CommonUtil::getInstance).thenReturn(serviceInstance);
    }

    @AfterEach
    void tearDown() {
        if (mockedCommonUtil != null) {
            mockedCommonUtil.close();
        }
        if (mockedSpringContextHolder != null) {
            mockedSpringContextHolder.close();
        }
    }

    @Test
    void testSendChangeMsg() {
        AbstractDtpNotifier notifier = new DtpDingNotifier(this.notifier);
        NotifyPlatform notifyPlatform = new NotifyPlatform();
        TpMainFields oldFields = new TpMainFields();
        List<String> diffs = Lists.newArrayList("corePoolSize");
        DtpNotifyCtxHolder.set(new NoticeCtx(ExecutorWrapper.of(dtpExecutor), new NotifyItem(), oldFields, diffs));
        notifier.sendChangeMsg(notifyPlatform, oldFields, diffs);

        Mockito.verify(this.notifier, Mockito.times(1)).send(any(), anyString());
    }

    @Test
    void testSendAlarmMsg() {
        AbstractDtpNotifier notifier = new DtpDingNotifier(this.notifier);
        NotifyPlatform notifyPlatform = new NotifyPlatform();
        NotifyItemEnum notifyItemEnum = NotifyItemEnum.LIVENESS;
        AlarmCtx alarmCtx = new AlarmCtx(ExecutorWrapper.of(dtpExecutor), new NotifyItem());
        alarmCtx.setAlarmInfo(new AlarmInfo());
        DtpNotifyCtxHolder.set(alarmCtx);
        notifier.sendAlarmMsg(notifyPlatform, notifyItemEnum);

        Mockito.verify(this.notifier, Mockito.times(1)).send(any(), anyString());
    }

    @Test
    void testGetQueueName() {
        assertEquals(VARIABLE_LINKED_BLOCKING_QUEUE.getName(), dtpExecutor.getQueueType());
    }

    @Test
    void testLarkSendChangeMsg() {
        DtpNotifier larkNotifier = new DtpLarkNotifier(new LarkNotifier());
        NotifyPlatform notifyPlatform = new NotifyPlatform();
        notifyPlatform.setWebhook("");
        notifyPlatform.setReceivers("");
        TpMainFields oldFields = new TpMainFields();
        List<String> diffs = Lists.newArrayList("corePoolSize");
        DtpNotifyCtxHolder.set(new NoticeCtx(ExecutorWrapper.of(dtpExecutor), new NotifyItem(), oldFields, diffs));
        assertNotNull(DtpNotifyCtxHolder.get());
        larkNotifier.sendChangeMsg(notifyPlatform, oldFields, diffs);
    }
}
