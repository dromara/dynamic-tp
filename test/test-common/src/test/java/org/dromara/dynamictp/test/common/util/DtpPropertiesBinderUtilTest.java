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

package org.dromara.dynamictp.test.common.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dromara.dynamictp.common.entity.DtpExecutorProps;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.entity.TpExecutorProps;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.DtpPropertiesBinderUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * DtpPropertiesBinderUtilTest related.
 */
class DtpPropertiesBinderUtilTest {

    @Test
    void testCustomExecutorInheritsGlobalBasicAndCollectionFieldsWhenLocalConfigAbsent() throws Exception {
        DtpProperties properties = newDtpProperties();
        DtpExecutorProps globalProps = globalProps();
        DtpExecutorProps executor = new DtpExecutorProps();
        Map<String, Object> source = new HashMap<>();
        source.put("dynamictp.globalExecutorProps.corePoolSize", 8);
        source.put("dynamictp.globalExecutorProps.maximumPoolSize", 16);
        source.put("dynamictp.globalExecutorProps.queueCapacity", 2048);
        source.put("dynamictp.globalExecutorProps.executorType", "common");
        source.put("dynamictp.globalExecutorProps.autoCreate", false);

        properties.setGlobalExecutorProps(globalProps);
        properties.setExecutors(Lists.newArrayList(executor));

        DtpPropertiesBinderUtil.tryResetWithGlobalConfig(source, properties);

        Assertions.assertEquals(8, executor.getCorePoolSize());
        Assertions.assertEquals(16, executor.getMaximumPoolSize());
        Assertions.assertEquals(2048, executor.getQueueCapacity());
        Assertions.assertEquals("common", executor.getExecutorType());
        Assertions.assertFalse(executor.isAutoCreate());
        Assertions.assertEquals(globalProps.getTaskWrapperNames(), executor.getTaskWrapperNames());
        Assertions.assertEquals(globalProps.getPlatformIds(), executor.getPlatformIds());
        Assertions.assertEquals(globalProps.getNotifyItems(), executor.getNotifyItems());
        Assertions.assertEquals(globalProps.getAwareNames(), executor.getAwareNames());
        Assertions.assertEquals(globalProps.getPluginNames(), executor.getPluginNames());
    }

    @Test
    void testCustomExecutorKeepsLocalBasicAndCollectionFieldsWhenLocalConfigPresent() throws Exception {
        DtpProperties properties = newDtpProperties();
        DtpExecutorProps globalProps = globalProps();
        DtpExecutorProps executor = new DtpExecutorProps();
        executor.setCorePoolSize(4);
        executor.setTaskWrapperNames(Sets.newHashSet("local-wrapper"));
        Map<String, Object> source = new HashMap<>();
        source.put("dynamictp.globalExecutorProps.corePoolSize", 8);
        source.put("dynamictp.executors[0].corePoolSize", 4);
        source.put("dynamictp.executors[0].taskWrapperNames[0]", "local-wrapper");

        properties.setGlobalExecutorProps(globalProps);
        properties.setExecutors(Lists.newArrayList(executor));

        DtpPropertiesBinderUtil.tryResetWithGlobalConfig(source, properties);

        Assertions.assertEquals(4, executor.getCorePoolSize());
        Assertions.assertEquals(Sets.newHashSet("local-wrapper"), executor.getTaskWrapperNames());
    }

    @Test
    void testAdapterExecutorsInheritGlobalFieldsForSingleAndListProperties() throws Exception {
        DtpProperties properties = newDtpProperties();
        DtpExecutorProps globalProps = globalProps();
        TpExecutorProps tomcatTp = new TpExecutorProps();
        TpExecutorProps dubboTp = new TpExecutorProps();
        dubboTp.setQueueCapacity(512);
        Map<String, Object> source = new HashMap<>();
        source.put("dynamictp.globalExecutorProps.queueCapacity", 2048);
        source.put("dynamictp.globalExecutorProps.runTimeout", 1000L);
        source.put("dynamictp.dubboTp[0].queueCapacity", 512);

        properties.setGlobalExecutorProps(globalProps);
        properties.setTomcatTp(tomcatTp);
        properties.setDubboTp(Lists.newArrayList(dubboTp));

        DtpPropertiesBinderUtil.tryResetWithGlobalConfig(source, properties);

        Assertions.assertEquals(2048, tomcatTp.getQueueCapacity());
        Assertions.assertEquals(1000L, tomcatTp.getRunTimeout());
        Assertions.assertEquals(globalProps.getPlatformIds(), tomcatTp.getPlatformIds());
        Assertions.assertEquals(512, dubboTp.getQueueCapacity());
        Assertions.assertEquals(1000L, dubboTp.getRunTimeout());
        Assertions.assertEquals(globalProps.getAwareNames(), dubboTp.getAwareNames());
    }

    @Test
    void testReturnsWithoutGlobalExecutorProps() throws Exception {
        DtpProperties properties = newDtpProperties();
        DtpExecutorProps executor = new DtpExecutorProps();
        Map<String, Object> source = new HashMap<>();
        source.put("dynamictp.globalExecutorProps.corePoolSize", 8);

        properties.setExecutors(Lists.newArrayList(executor));

        DtpPropertiesBinderUtil.tryResetWithGlobalConfig(source, properties);

        Assertions.assertEquals(1, executor.getCorePoolSize());
    }

    private static DtpProperties newDtpProperties() throws Exception {
        Constructor<DtpProperties> constructor = DtpProperties.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static DtpExecutorProps globalProps() {
        NotifyItem notifyItem = new NotifyItem();
        notifyItem.setType("capacity");

        DtpExecutorProps globalProps = new DtpExecutorProps();
        globalProps.setTaskWrapperNames(Sets.newHashSet("mdc", "ttl"));
        globalProps.setPlatformIds(Lists.newArrayList("ding"));
        globalProps.setNotifyItems(Lists.newArrayList(notifyItem));
        globalProps.setAwareNames(Lists.newArrayList("taskStat"));
        globalProps.setPluginNames(Sets.newHashSet("monitor"));
        return globalProps;
    }
}
