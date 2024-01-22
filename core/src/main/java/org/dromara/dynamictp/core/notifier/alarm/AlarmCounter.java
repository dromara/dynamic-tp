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
import lombok.val;
import lombok.var;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.AlarmInfo;
import org.dromara.dynamictp.core.support.ExecutorWrapper;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.UNKNOWN;

/**
 * AlarmCounter related
 *
 * @author yanhom
 * @since 1.0.4
 **/
public class AlarmCounter {

    private static final Map<String, AlarmInfo> ALARM_INFO_CACHE = new ConcurrentHashMap<>();

    private AlarmCounter() { }

    public static void init(String threadPoolName, String notifyItemType) {
        String key = buildKey(threadPoolName, notifyItemType);
        val alarmInfo = new AlarmInfo().setNotifyItem(NotifyItemEnum.of(notifyItemType));
        ALARM_INFO_CACHE.putIfAbsent(key, alarmInfo);
    }

    public static AlarmInfo getAlarmInfo(String threadPoolName, String notifyItemType) {
        String key = buildKey(threadPoolName, notifyItemType);
        return ALARM_INFO_CACHE.get(key);
    }

    public static String getCount(String threadPoolName, String notifyItemType) {
        String key = buildKey(threadPoolName, notifyItemType);
        val alarmInfo = ALARM_INFO_CACHE.get(key);
        if (Objects.nonNull(alarmInfo)) {
            return String.valueOf(alarmInfo.getCount());
        }
        return UNKNOWN;
    }

    public static void reset(String threadPoolName, String notifyItemType) {
        String key = buildKey(threadPoolName, notifyItemType);
        var alarmInfo = ALARM_INFO_CACHE.get(key);
        alarmInfo.reset();
    }

    public static void incAlarmCounter(String threadPoolName, String notifyItemType) {
        String key = buildKey(threadPoolName, notifyItemType);
        var alarmInfo = ALARM_INFO_CACHE.get(key);
        if (Objects.nonNull(alarmInfo)) {
            alarmInfo.incCounter();
        }
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
                return Integer.parseInt(getCount(wrapper.getThreadPoolName(), itemEnum.getValue()));
            default:
                return 0;
        }
    }

    private static String buildKey(String threadPoolName, String notifyItemType) {
        return threadPoolName + ":" + notifyItemType;
    }
}
