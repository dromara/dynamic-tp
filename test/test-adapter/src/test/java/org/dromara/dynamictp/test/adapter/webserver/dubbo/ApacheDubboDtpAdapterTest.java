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

package org.dromara.dynamictp.test.adapter.webserver.dubbo;

import lombok.val;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * dubbo adapter test
 *
 * @author <a href = "mailto:kamtohung@gmail.com">hongjintao</a>
 */
public class ApacheDubboDtpAdapterTest {

    private final ConcurrentMap<String, ConcurrentMap<String, ExecutorService>> newData = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, ConcurrentMap<Integer, ExecutorService>> oldData = new ConcurrentHashMap<>();

    @Test
    @SuppressWarnings("unchecked")
    public void testOldDubboConcurrentMap() {
        ApacheDubboDtpAdapterTest targetObj = new ApacheDubboDtpAdapterTest();
        ConcurrentHashMap<String, ExecutorService> newValue = new ConcurrentHashMap<>();
        newValue.put("first-pool", Executors.newFixedThreadPool(1));
        targetObj.newData.put("name", newValue);
        val newData = (ConcurrentMap<String, ConcurrentMap<Object, ExecutorService>>) ReflectionUtil.getFieldValue(
                ApacheDubboDtpAdapterTest.class, "newData", targetObj);
        assert newData != null;
        Assertions.assertEquals("first-pool", newData.get("name").keySet().iterator().next());

        ConcurrentHashMap<Integer, ExecutorService> oldValue = new ConcurrentHashMap<>();
        oldValue.put(123, Executors.newFixedThreadPool(1));
        targetObj.oldData.put("name", oldValue);

        val oldData = (ConcurrentMap<String, ConcurrentMap<Object, ExecutorService>>) ReflectionUtil.getFieldValue(
                ApacheDubboDtpAdapterTest.class, "oldData", targetObj);
        assert oldData != null;
        Assertions.assertEquals(123, oldData.get("name").keySet().iterator().next());
    }

}
