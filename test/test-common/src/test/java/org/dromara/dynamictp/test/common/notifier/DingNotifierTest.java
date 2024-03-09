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
package org.dromara.dynamictp.test.common.notifier;

import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.notifier.DingNotifier;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
public class DingNotifierTest {

    @Test
    public void buildUrlWithWebhook() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        NotifyPlatform notifyPlatform = new NotifyPlatform();
        notifyPlatform.setWebhook("https://oapi.dingtalk.com/robot/send?access_token=123");
//        notifyPlatform.setUrlKey("123");
//        notifyPlatform.setSecret("456");
        DingNotifier dingNotifier = new DingNotifier();
        Method privateMethod = DingNotifier.class.getDeclaredMethod("getTargetUrl", String.class, String.class, String.class);
        privateMethod.setAccessible(true);
        String result = (String) privateMethod.invoke(dingNotifier, notifyPlatform.getSecret(), notifyPlatform.getUrlKey(), notifyPlatform.getWebhook());
        Assertions.assertEquals("https://oapi.dingtalk.com/robot/send?access_token=123", result);
    }

    @Test
    public void buildUrlWithUrlKey() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        NotifyPlatform notifyPlatform = new NotifyPlatform();
        notifyPlatform.setWebhook("https://oapi.dingtalk.com/robot/send?access_token=123");
        notifyPlatform.setUrlKey("123");
//        notifyPlatform.setSecret("456");
        DingNotifier dingNotifier = new DingNotifier();
        Method privateMethod = DingNotifier.class.getDeclaredMethod("getTargetUrl", String.class, String.class, String.class);
        privateMethod.setAccessible(true);
        String result = (String) privateMethod.invoke(dingNotifier, notifyPlatform.getSecret(), notifyPlatform.getUrlKey(), notifyPlatform.getWebhook());
        Assertions.assertEquals("https://oapi.dingtalk.com/robot/send?access_token=123", result);
    }


    @Test
    public void buildUrlWithSecret() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        NotifyPlatform notifyPlatform = new NotifyPlatform();
        notifyPlatform.setWebhook("https://oapi.dingtalk.com/robot/send?access_token=123");
//        notifyPlatform.setUrlKey("123");
        notifyPlatform.setSecret("456");
        DingNotifier dingNotifier = new DingNotifier();
        Method privateMethod = DingNotifier.class.getDeclaredMethod("getTargetUrl", String.class, String.class, String.class);
        privateMethod.setAccessible(true);
        String result = (String) privateMethod.invoke(dingNotifier, notifyPlatform.getSecret(), notifyPlatform.getUrlKey(), notifyPlatform.getWebhook());
        System.out.println(result);
        Assertions.assertTrue(result.contains("timestamp="));
        Assertions.assertTrue(result.contains("sign="));
    }

}
