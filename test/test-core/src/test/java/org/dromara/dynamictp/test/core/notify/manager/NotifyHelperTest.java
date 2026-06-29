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
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.notifier.manager.NotifyHelper;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NotifyHelper test.
 *
 * @author codex
 */
@Execution(ExecutionMode.SAME_THREAD)
class NotifyHelperTest {

    private DtpProperties properties;

    private GenericApplicationContext context;

    private ApplicationContext originalContext;

    private ThreadPoolExecutor executor;

    @BeforeEach
    void setUp() throws Exception {
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
        executor.shutdownNow();
        context.close();
        setSpringContext(originalContext);
    }

    @Test
    void testGetNotifyItemMatchesTypeIgnoringCase() {
        NotifyItem liveness = notifyItem(NotifyItemEnum.LIVENESS.getValue().toUpperCase());
        NotifyItem reject = notifyItem(NotifyItemEnum.REJECT.getValue());
        ExecutorWrapper wrapper = executorWrapper(liveness, reject);

        Optional<NotifyItem> result = NotifyHelper.getNotifyItem(wrapper, NotifyItemEnum.LIVENESS);

        assertTrue(result.isPresent());
        assertSame(liveness, result.get());
    }

    @Test
    void testGetNotifyItemReturnsEmptyWhenItemsEmptyOrMissing() {
        ExecutorWrapper emptyWrapper = executorWrapper();
        ExecutorWrapper missingWrapper = executorWrapper(notifyItem(NotifyItemEnum.REJECT.getValue()));

        assertFalse(NotifyHelper.getNotifyItem(emptyWrapper, NotifyItemEnum.LIVENESS).isPresent());
        assertFalse(NotifyHelper.getNotifyItem(missingWrapper, NotifyItemEnum.LIVENESS).isPresent());
    }

    @Test
    void testGetPlatformReturnsConfiguredPlatform() {
        NotifyPlatform lark = platform("lark-platform");
        NotifyPlatform ding = platform("ding-platform");
        properties.setPlatforms(Arrays.asList(lark, ding));

        Optional<NotifyPlatform> result = NotifyHelper.getPlatform("ding-platform");

        assertTrue(result.isPresent());
        assertSame(ding, result.get());
    }

    @Test
    void testGetPlatformReturnsEmptyWhenPlatformsEmptyOrMissing() {
        assertFalse(NotifyHelper.getPlatform("missing").isPresent());

        properties.setPlatforms(Collections.singletonList(platform("lark-platform")));

        assertFalse(NotifyHelper.getPlatform("missing").isPresent());
    }

    private ExecutorWrapper executorWrapper(NotifyItem... notifyItems) {
        ExecutorWrapper wrapper = new ExecutorWrapper("notify-helper-test", executor);
        wrapper.setNotifyItems(Arrays.asList(notifyItems));
        return wrapper;
    }

    private NotifyItem notifyItem(String type) {
        NotifyItem notifyItem = new NotifyItem();
        notifyItem.setType(type);
        return notifyItem;
    }

    private NotifyPlatform platform(String platformId) {
        NotifyPlatform platform = new NotifyPlatform();
        platform.setPlatformId(platformId);
        return platform;
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
}
