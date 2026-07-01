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

package org.dromara.dynamictp.test.core.notify.invoker;

import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.entity.TpMainFields;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.handler.NotifierHandler;
import org.dromara.dynamictp.core.notifier.DtpNotifier;
import org.dromara.dynamictp.core.notifier.alarm.AlarmCounter;
import org.dromara.dynamictp.core.notifier.chain.invoker.AlarmInvoker;
import org.dromara.dynamictp.core.notifier.context.AlarmCtx;
import org.dromara.dynamictp.core.notifier.context.DtpNotifyCtxHolder;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.spring.holder.SpringContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * AlarmInvoker test.
 *
 * @author codex
 */
@Execution(ExecutionMode.SAME_THREAD)
class AlarmInvokerTest {

    private static final String PLATFORM_TYPE = "test_alarm_invoker";

    private Map<String, DtpNotifier> notifiers;

    private DtpProperties properties;

    private GenericApplicationContext context;

    private ApplicationContext originalContext;

    private ThreadPoolExecutor executor;

    @BeforeEach
    void setUp() throws Exception {
        notifiers = notifiers();
        properties = newProperties();
        originalContext = springContext();
        context = new GenericApplicationContext();
        context.getBeanFactory().registerSingleton("dtpProperties", properties);
        context.refresh();
        new SpringContextHolder().setApplicationContext(context);
        executor = new ThreadPoolExecutor(
                1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    @AfterEach
    void tearDown() throws Exception {
        notifiers.remove(PLATFORM_TYPE);
        DtpNotifyCtxHolder.remove();
        executor.shutdownNow();
        context.close();
        setSpringContext(originalContext);
    }

    @Test
    void testInvokeSendsAlarmResetsCounterAndClearsContext() {
        RecordingNotifier notifier = new RecordingNotifier();
        NotifyItem notifyItem = notifyItem(NotifyItemEnum.REJECT);
        String poolName = "alarm-invoker-success";
        properties.setPlatforms(Collections.singletonList(platform()));
        notifiers.put(PLATFORM_TYPE, notifier);
        AlarmCounter.initAlarmCounter(poolName, notifyItem);
        AlarmCounter.incAlarmCount(poolName, notifyItem.getType());
        AlarmCounter.incAlarmCount(poolName, notifyItem.getType());

        new AlarmInvoker().invoke(new AlarmCtx(new ExecutorWrapper(poolName, executor), notifyItem));

        assertEquals(NotifyItemEnum.REJECT, notifier.notifyItemEnum);
        assertEquals(0, AlarmCounter.getAlarmInfo(poolName, notifyItem.getType()).getCount());
        assertEquals(platform().getPlatformId(), notifier.platform.getPlatformId());
        assertNull(DtpNotifyCtxHolder.get());
    }

    @Test
    void testInvokeClearsContextWhenSendAlarmFails() {
        NotifyItem notifyItem = notifyItem(NotifyItemEnum.REJECT);
        String poolName = "alarm-invoker-fail";
        properties.setPlatforms(Collections.singletonList(platform()));
        notifiers.put(PLATFORM_TYPE, new FailingNotifier());
        AlarmCounter.initAlarmCounter(poolName, notifyItem);

        assertThrows(IllegalStateException.class,
                () -> new AlarmInvoker().invoke(new AlarmCtx(new ExecutorWrapper(poolName, executor), notifyItem)));

        assertNull(DtpNotifyCtxHolder.get());
    }

    private NotifyItem notifyItem(NotifyItemEnum notifyItemEnum) {
        NotifyItem notifyItem = new NotifyItem();
        notifyItem.setType(notifyItemEnum.getValue());
        notifyItem.setPlatformIds(Collections.singletonList("alarm-invoker-platform"));
        notifyItem.setPeriod(120);
        return notifyItem;
    }

    private NotifyPlatform platform() {
        NotifyPlatform platform = new NotifyPlatform();
        platform.setPlatformId("alarm-invoker-platform");
        platform.setPlatform(PLATFORM_TYPE.toUpperCase());
        return platform;
    }

    @SuppressWarnings("unchecked")
    private Map<String, DtpNotifier> notifiers() throws Exception {
        Field field = NotifierHandler.class.getDeclaredField("NOTIFIERS");
        field.setAccessible(true);
        return (Map<String, DtpNotifier>) field.get(null);
    }

    private DtpProperties newProperties() throws Exception {
        Constructor<DtpProperties> constructor = DtpProperties.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private ApplicationContext springContext() throws Exception {
        Field field = SpringContextHolder.class.getDeclaredField("context");
        field.setAccessible(true);
        return (ApplicationContext) field.get(null);
    }

    private void setSpringContext(ApplicationContext applicationContext) throws Exception {
        Field field = SpringContextHolder.class.getDeclaredField("context");
        field.setAccessible(true);
        field.set(null, applicationContext);
    }

    private static class RecordingNotifier implements DtpNotifier {

        private NotifyPlatform platform;

        private NotifyItemEnum notifyItemEnum;

        @Override
        public String platform() {
            return PLATFORM_TYPE;
        }

        @Override
        public void sendChangeMsg(NotifyPlatform notifyPlatform, TpMainFields oldFields, List<String> diffs) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sendAlarmMsg(NotifyPlatform notifyPlatform, NotifyItemEnum notifyItemEnum) {
            this.platform = notifyPlatform;
            this.notifyItemEnum = notifyItemEnum;
        }
    }

    private static class FailingNotifier extends RecordingNotifier {

        @Override
        public void sendAlarmMsg(NotifyPlatform notifyPlatform, NotifyItemEnum notifyItemEnum) {
            throw new IllegalStateException("alarm failed");
        }
    }
}
