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

package org.dromara.dynamictp.test.common.properties;

import org.dromara.dynamictp.common.em.CollectorTypeEnum;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * DtpPropertiesTest related.
 */
class DtpPropertiesTest {

    @Test
    void testGetInstanceReturnsSingletonWithDefaults() {
        DtpProperties properties = DtpProperties.getInstance();

        Assertions.assertSame(properties, DtpProperties.getInstance());
        Assertions.assertTrue(properties.isEnabled());
        Assertions.assertTrue(properties.isEnabledBanner());
        Assertions.assertTrue(properties.isEnabledCollect());
        Assertions.assertEquals(CollectorTypeEnum.MICROMETER.name(), properties.getCollectorTypes().get(0));
        Assertions.assertEquals(5, properties.getMonitorInterval());
    }

    @Test
    void testEtcdDefaults() {
        DtpProperties.Etcd etcd = new DtpProperties.Etcd();

        Assertions.assertEquals("UTF-8", etcd.getCharset());
        Assertions.assertFalse(etcd.isAuthEnable());
        Assertions.assertEquals("ssl", etcd.getAuthority());
        Assertions.assertEquals(30000L, etcd.getTimeout());
    }

    @Test
    void testZookeeperAccessors() {
        DtpProperties.Zookeeper zookeeper = new DtpProperties.Zookeeper();

        zookeeper.setZkConnectStr("127.0.0.1:2181");
        zookeeper.setConfigVersion("v1");
        zookeeper.setRootNode("/dtp");
        zookeeper.setNode("node");
        zookeeper.setConfigKey("config");

        Assertions.assertEquals("127.0.0.1:2181", zookeeper.getZkConnectStr());
        Assertions.assertEquals("v1", zookeeper.getConfigVersion());
        Assertions.assertEquals("/dtp", zookeeper.getRootNode());
        Assertions.assertEquals("node", zookeeper.getNode());
        Assertions.assertEquals("config", zookeeper.getConfigKey());
    }
}
