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
import org.dromara.dynamictp.common.entity.AlarmInfo;
import org.dromara.dynamictp.core.support.ExecutorAdapter;
import org.dromara.dynamictp.core.thread.DtpExecutor;
import lombok.val;
import lombok.var;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.UNKNOWN;
import static org.dromara.dynamictp.common.em.NotifyItemEnum.QUEUE_TIMEOUT;
import static org.dromara.dynamictp.common.em.NotifyItemEnum.REJECT;
import static org.dromara.dynamictp.common.em.NotifyItemEnum.RUN_TIMEOUT;

/**
 * AlarmCounter related
 *
 * @author yanhom
 * @since 1.0.4
 **/
public class AlarmCounter {

    private static final String UNKNOWN_COUNT_STR = UNKNOWN + " / " + UNKNOWN;

    private AlarmCounter() { }

    private static final Map<String, AlarmInfo> ALARM_INFO_CACHE = new ConcurrentHashMap<>();

    public static void init(String threadPoolName, String notifyItemType) {
        String key = buildKey(threadPoolName, notifyItemType);
        val alarmInfo = AlarmInfo.builder()
                .notifyItem(NotifyItemEnum.of(notifyItemType))
                .build();
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

    public static Triple<String, String, String> countStrRrq(String threadPoolName, ExecutorAdapter<?> executor) {

        if (!(executor.getOriginal() instanceof DtpExecutor)) {
            return new ImmutableTriple<>(UNKNOWN_COUNT_STR, UNKNOWN_COUNT_STR, UNKNOWN_COUNT_STR);
        }

        DtpExecutor dtpExecutor = (DtpExecutor) executor.getOriginal();
        String rejectCount = getCount(threadPoolName, REJECT.getValue()) + " / " + dtpExecutor.getRejectedTaskCount();
        String runTimeoutCount = getCount(threadPoolName, RUN_TIMEOUT.getValue()) + " / "
                + dtpExecutor.getRunTimeoutCount();
        String queueTimeoutCount = getCount(threadPoolName, QUEUE_TIMEOUT.getValue()) + " / "
                + dtpExecutor.getQueueTimeoutCount();
        return new ImmutableTriple<>(rejectCount, runTimeoutCount, queueTimeoutCount);
    }

    private static String buildKey(String threadPoolName, String notifyItemType) {
        return threadPoolName + ":" + notifyItemType;
    }
}
