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

package org.dromara.dynamictp.test.common.entity;

import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.Proxy;
import java.util.UUID;

/**
 * NotifyPlatformTest related.
 */
class NotifyPlatformTest {

    @Test
    void testDefaultsAreInitialized() {
        NotifyPlatform platform = new NotifyPlatform();

        Assertions.assertDoesNotThrow(() -> UUID.fromString(platform.getPlatformId()));
        Assertions.assertEquals("all", platform.getReceivers());
        Assertions.assertEquals(Integer.valueOf(3000), platform.getTimeout());
        Assertions.assertEquals(Proxy.Type.DIRECT, platform.getProxyType());
        Assertions.assertEquals(0, platform.getProxyPort());
        Assertions.assertNull(platform.getPlatform());
        Assertions.assertNull(platform.getUrlKey());
        Assertions.assertNull(platform.getSecret());
        Assertions.assertNull(platform.getWebhook());
        Assertions.assertNull(platform.getProxyHost());
    }

    @Test
    void testNotifyPlatformPropertiesCanBeSetAndCompared() {
        NotifyPlatform first = new NotifyPlatform();
        first.setPlatformId("platform-id");
        first.setPlatform("ding");
        first.setUrlKey("url-key");
        first.setSecret("secret");
        first.setWebhook("webhook");
        first.setReceivers("userA,userB");
        first.setTimeout(5000);
        first.setProxyType(Proxy.Type.HTTP);
        first.setProxyHost("127.0.0.1");
        first.setProxyPort(8080);

        NotifyPlatform second = new NotifyPlatform();
        second.setPlatformId("platform-id");
        second.setPlatform("ding");
        second.setUrlKey("url-key");
        second.setSecret("secret");
        second.setWebhook("webhook");
        second.setReceivers("userA,userB");
        second.setTimeout(5000);
        second.setProxyType(Proxy.Type.HTTP);
        second.setProxyHost("127.0.0.1");
        second.setProxyPort(8080);

        Assertions.assertEquals(first, second);
        Assertions.assertEquals(first.hashCode(), second.hashCode());
        Assertions.assertTrue(first.toString().contains("platform=ding"));
    }
}
