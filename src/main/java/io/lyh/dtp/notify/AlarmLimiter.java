package io.lyh.dtp.notify;

import io.lyh.dtp.core.DtpExecutor;
import io.lyh.dtp.domain.NotifyItem;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * AlarmLimiter related
 *
 * @author: yanhom1314@gmail.com
 * @date 2022-01-03 上午12:21
 */
public class AlarmLimiter {

    private static final Map<String, Cache<String, String>> ALARM_LIMITER = new ConcurrentHashMap<>();

    private AlarmLimiter() {}

    public static void initAlarmLimiter(String dtpName, NotifyItem notifyItem) {
        String outerKey = buildOuterKey(dtpName, notifyItem.getType());
        if (ALARM_LIMITER.get(outerKey) != null) {
            return;
        }
        Cache<String, String> cache = CacheBuilder.newBuilder()
                .expireAfterWrite(notifyItem.getInterval(), TimeUnit.SECONDS)
                .build();
        ALARM_LIMITER.put(outerKey, cache);
    }

    public static void putVal(DtpExecutor executor, String type) {
        String outerKey = buildOuterKey(executor.getThreadPoolName(), type);
        ALARM_LIMITER.get(outerKey).put(type, type);
    }

    public static String getAlarmLimitInfo(String outerKey, String innerKey) {
        return ALARM_LIMITER.get(outerKey).getIfPresent(innerKey);
    }

    public static boolean ifAlarm(DtpExecutor executor, String type) {
        String key = buildOuterKey(executor.getThreadPoolName(), type);
        return StringUtils.isBlank(getAlarmLimitInfo(key, type));
    }

    public static String buildOuterKey(String dtpName, String type) {
        return dtpName + ":" + type;
    }
}
