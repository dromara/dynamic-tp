package com.dtp.core.notify;

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
 * @author: yanhom
 * @since 1.0.4
 **/
public class AlarmCounter {

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

    public static Triple<String, String, String> countRrq(String threadPoolName, ThreadPoolExecutor executor) {

        String rejectCount;
        if (executor instanceof DtpExecutor) {
            val rejectAlarm = getAlarmInfo(threadPoolName, REJECT.getValue());
            val leftCount = rejectAlarm == null ? UNKNOWN : String.valueOf(rejectAlarm.getCount());
            rejectCount = leftCount + "/" + ((DtpExecutor) executor).getRejectCount();
        } else {
            rejectCount = UNKNOWN + "/" + UNKNOWN;
        }

        String runTimeoutCount;
        if (executor instanceof DtpExecutor) {
            val runTimeoutAlarm = getAlarmInfo(threadPoolName, RUN_TIMEOUT.getValue());
            val leftCount = runTimeoutAlarm == null ? UNKNOWN : String.valueOf(runTimeoutAlarm.getCount());
            runTimeoutCount = leftCount + "/" + ((DtpExecutor) executor).getRunTimeoutCount();
        } else {
            runTimeoutCount = UNKNOWN + "/" + UNKNOWN;
        }

        String queueTimeoutCount;
        if (executor instanceof DtpExecutor) {
            val queueTimeoutAlarm = getAlarmInfo(threadPoolName, QUEUE_TIMEOUT.getValue());
            val leftCount = queueTimeoutAlarm == null ? UNKNOWN : String.valueOf(queueTimeoutAlarm.getCount());
            queueTimeoutCount = leftCount + "/" + ((DtpExecutor) executor).getQueueTimeoutCount();
        } else {
            queueTimeoutCount = UNKNOWN + "/" + UNKNOWN;
        }

        return new ImmutableTriple<>(rejectCount, runTimeoutCount, queueTimeoutCount);
    }

    private static String buildKey(String threadPoolName, String notifyType) {
        return threadPoolName + ":" + notifyType;
    }
}
