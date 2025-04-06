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

import cn.hutool.core.util.NumberUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.val;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.AlarmInfo;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.util.DateUtil;
import org.dromara.dynamictp.core.support.ExecutorWrapper;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * AlarmCounter related
 *
 * @author yanhom
 * @since 1.0.4
 **/
public class AlarmCounter {

    private static final Map<String, Cache<String, AlarmInfo>> ALARM_INFO_CACHE = new ConcurrentHashMap<>();

    private static final Map<String, String> LAST_ALARM_TIME_MAP = new ConcurrentHashMap<>();

    private AlarmCounter() { }

    public static void initAlarmCounter(String threadPoolName, NotifyItem notifyItem) {
        if (NotifyItemEnum.CHANGE.getValue().equalsIgnoreCase(notifyItem.getType())) {
            return;
        }

        String key = buildKey(threadPoolName, notifyItem.getType());
        Cache<String, AlarmInfo> cache = CacheBuilder.newBuilder()
                .expireAfterWrite(notifyItem.getPeriod(), TimeUnit.SECONDS)
                .build();
        ALARM_INFO_CACHE.put(key, cache);
    }

    public static AlarmInfo getAlarmInfo(String threadPoolName, String notifyType) {
        String key = buildKey(threadPoolName, notifyType);
        val alarmInfo = ALARM_INFO_CACHE.get(key);
        if (Objects.isNull(alarmInfo)) {
            return null;
        }
        return alarmInfo.getIfPresent(notifyType);
    }

    public static int getCount(String threadPoolName, String notifyType) {
        val alarmInfo = getAlarmInfo(threadPoolName, notifyType);
        if (Objects.nonNull(alarmInfo)) {
            return alarmInfo.getCount();
        }
        return 0;
    }

    public static void reset(String threadPoolName, String notifyType) {
        val alarmInfo = getAlarmInfo(threadPoolName, notifyType);
        if (Objects.nonNull(alarmInfo)) {
            alarmInfo.reset();
        }
        String key = buildKey(threadPoolName, notifyType);
        LAST_ALARM_TIME_MAP.put(key, DateUtil.now());
    }

    public static String getLastAlarmTime(String threadPoolName, String notifyType) {
        String key = buildKey(threadPoolName, notifyType);
        return LAST_ALARM_TIME_MAP.get(key);
    }

    public static void incAlarmCount(String threadPoolName, String notifyType) {
        AlarmInfo alarmInfo = getAlarmInfo(threadPoolName, notifyType);
        if (Objects.isNull(alarmInfo)) {
            String key = buildKey(threadPoolName, notifyType);
            alarmInfo = new AlarmInfo().setNotifyItem(NotifyItemEnum.of(notifyType));
            ALARM_INFO_CACHE.get(key).put(notifyType, alarmInfo);
        }
        alarmInfo.incCounter();
    }

    public static int calcCurrentValue(ExecutorWrapper wrapper, NotifyItemEnum itemEnum) {
        val executor = wrapper.getExecutor();
        switch (itemEnum) {
            case CAPACITY:
                return (int) (NumberUtil.div(executor.getQueueSize(), executor.getQueueCapacity(), 2) * 100);
            case LIVENESS:
                return (int) (NumberUtil.div(executor.getActiveCount(), executor.getMaximumPoolSize(), 2) * 100);
            case REJECT:
            case RUN_TIMEOUT:
            case QUEUE_TIMEOUT:
                return getCount(wrapper.getThreadPoolName(), itemEnum.getValue());
            default:
                return 0;
        }
    }

    private static String buildKey(String threadPoolName, String notifyItemType) {
        return threadPoolName + ":" + notifyItemType;
    }
}
