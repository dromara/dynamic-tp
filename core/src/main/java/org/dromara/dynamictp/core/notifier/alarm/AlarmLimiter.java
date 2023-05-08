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

package org.dromara.dynamictp.core.notifier.alarm;

import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.NotifyItem;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * AlarmLimiter related
 *
 * @author yanhom
 * @since 1.0.0
 */
public class AlarmLimiter {

    private static final Map<String, Cache<String, String>> ALARM_LIMITER = new ConcurrentHashMap<>();

    private AlarmLimiter() { }

    public static void initAlarmLimiter(String threadPoolName, NotifyItem notifyItem) {
        if (NotifyItemEnum.CHANGE.getValue().equalsIgnoreCase(notifyItem.getType())) {
            return;
        }

        String key = genKey(threadPoolName, notifyItem.getType());
        Cache<String, String> cache = CacheBuilder.newBuilder()
                .expireAfterWrite(notifyItem.getInterval(), TimeUnit.SECONDS)
                .build();
        ALARM_LIMITER.put(key, cache);
    }

    public static void putVal(String threadPoolName, String type) {
        String key = genKey(threadPoolName, type);
        ALARM_LIMITER.get(key).put(type, type);
    }

    public static String getAlarmLimitInfo(String key, String type) {
        val cache = ALARM_LIMITER.get(key);
        if (Objects.isNull(cache)) {
            return null;
        }
        return cache.getIfPresent(type);
    }

    public static boolean ifAlarm(String threadPoolName, String type) {
        String key = genKey(threadPoolName, type);
        return StringUtils.isBlank(getAlarmLimitInfo(key, type));
    }

    public static String genKey(String threadPoolName, String type) {
        return threadPoolName + ":" + type;
    }
}
