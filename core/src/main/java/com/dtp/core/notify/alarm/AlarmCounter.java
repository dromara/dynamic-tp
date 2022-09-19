package com.dtp.core.notify.alarm;

import com.dtp.common.dto.AlarmInfo;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.core.thread.DtpExecutor;
import lombok.val;
import lombok.var;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import static com.dtp.common.constant.DynamicTpConst.UNKNOWN;
import static com.dtp.common.em.NotifyTypeEnum.*;

/**
 * AlarmCounter related
 *
 * @author yanhom
 * @since 1.0.4
 **/
public class AlarmCounter {

    private static final String DEFAULT_COUNT_STR = UNKNOWN + " / " + UNKNOWN;

    private AlarmCounter() {}

    private static final Map<String, AlarmInfo> ALARM_INFO_CACHE = new ConcurrentHashMap<>();

    public static void init(String threadPoolName, String notifyType) {
        String key = buildKey(threadPoolName, notifyType);
        val alarmInfo = AlarmInfo.builder()
                .type(NotifyTypeEnum.of(notifyType))
                .build();
        ALARM_INFO_CACHE.putIfAbsent(key, alarmInfo);
    }

    public static AlarmInfo getAlarmInfo(String threadPoolName, String notifyType) {
        String key = buildKey(threadPoolName, notifyType);
        return ALARM_INFO_CACHE.get(key);
    }

    public static String getCount(String threadPoolName, String notifyType) {
        String key = buildKey(threadPoolName, notifyType);
        val alarmInfo = ALARM_INFO_CACHE.get(key);
        if (Objects.nonNull(alarmInfo)) {
            return String.valueOf(alarmInfo.getCount());
        }
        return UNKNOWN;
    }

    public static void reset(String threadPoolName, String notifyType) {
        String key = buildKey(threadPoolName, notifyType);
        var alarmInfo = ALARM_INFO_CACHE.get(key);
        alarmInfo.reset();
    }

    public static void incAlarmCounter(String threadPoolName, String notifyType) {
        String key = buildKey(threadPoolName, notifyType);
        var alarmInfo = ALARM_INFO_CACHE.get(key);
        if (Objects.nonNull(alarmInfo)) {
            alarmInfo.incCounter();
        }
    }

    public static Triple<String, String, String> countStrRrq(String threadPoolName, ThreadPoolExecutor executor) {

        if (!(executor instanceof DtpExecutor)) {
            return new ImmutableTriple<>(DEFAULT_COUNT_STR, DEFAULT_COUNT_STR, DEFAULT_COUNT_STR);
        }

        DtpExecutor dtpExecutor = (DtpExecutor) executor;
        String rejectCount = getCount(threadPoolName, REJECT.getValue()) + " / " + dtpExecutor.getRejectCount();
        String runTimeoutCount = getCount(threadPoolName, RUN_TIMEOUT.getValue()) + " / " +
                dtpExecutor.getRunTimeoutCount();
        String queueTimeoutCount = getCount(threadPoolName, QUEUE_TIMEOUT.getValue()) + " / " +
                dtpExecutor.getQueueTimeoutCount();
        return new ImmutableTriple<>(rejectCount, runTimeoutCount, queueTimeoutCount);
    }

    private static String buildKey(String threadPoolName, String notifyType) {
        return threadPoolName + ":" + notifyType;
    }
}
