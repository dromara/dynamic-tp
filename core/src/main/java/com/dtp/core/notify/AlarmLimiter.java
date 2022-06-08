package com.dtp.core.notify;

import com.dtp.common.dto.NotifyItem;
import com.dtp.common.em.NotifyTypeEnum;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * AlarmLimiter related
 *
 * @author: yanhom
 * @since 1.0.0
 */
public class AlarmLimiter {

    private static final Map<String, Cache<String, String>> ALARM_LIMITER = new ConcurrentHashMap<>();

    private AlarmLimiter() {}

    public static void initAlarmLimiter(String threadPoolName, NotifyItem notifyItem) {
        if (NotifyTypeEnum.CHANGE.getValue().equalsIgnoreCase(notifyItem.getType())) {
            return;
        }

        String outerKey = buildOuterKey(threadPoolName, notifyItem.getType());
        Cache<String, String> cache = CacheBuilder.newBuilder()
                .expireAfterWrite(notifyItem.getInterval(), TimeUnit.SECONDS)
                .build();
        ALARM_LIMITER.put(outerKey, cache);
    }

    public static void putVal(String threadPoolName, String type) {
        String outerKey = buildOuterKey(threadPoolName, type);
        ALARM_LIMITER.get(outerKey).put(type, type);
    }

    public static String getAlarmLimitInfo(String outerKey, String innerKey) {
        return ALARM_LIMITER.get(outerKey).getIfPresent(innerKey);
    }

    public static boolean ifAlarm(String threadPoolName, String type) {
        String key = buildOuterKey(threadPoolName, type);
        return StringUtils.isBlank(getAlarmLimitInfo(key, type));
    }

    public static String buildOuterKey(String threadPoolName, String type) {
        return threadPoolName + ":" + type;
    }
}
