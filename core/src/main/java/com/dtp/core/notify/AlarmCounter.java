package com.dtp.core.notify;

import com.dtp.common.dto.AlarmInfo;
import com.dtp.common.em.NotifyTypeEnum;
import lombok.val;
import lombok.var;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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

    public static void init(String dtpName, String notifyType) {
        String key = buildKey(dtpName, notifyType);
        val alarmInfo = AlarmInfo.builder()
                .type(NotifyTypeEnum.of(notifyType))
                .build();
        ALARM_INFO_CACHE.putIfAbsent(key, alarmInfo);
    }

    public static AlarmInfo getAlarmInfo(String dtpName, String notifyType) {
        String key = buildKey(dtpName, notifyType);
        return ALARM_INFO_CACHE.get(key);
    }

    public static void reset(String dtpName, String notifyType) {
        String key = buildKey(dtpName, notifyType);
        var alarmInfo = ALARM_INFO_CACHE.get(key);
        alarmInfo.reset();
    }

    public static void incAlarmCounter(String dtpName, String notifyType) {
        String key = buildKey(dtpName, notifyType);
        var alarmInfo = ALARM_INFO_CACHE.get(key);
        if (Objects.nonNull(alarmInfo)) {
            alarmInfo.incCounter();
        }
    }

    public static Triple<String, String, String> countNotifyItems(String dtpName) {
        val rejectAlarm = getAlarmInfo(dtpName, REJECT.getValue());
        String rejectCount = rejectAlarm == null ? UNKNOWN : String.valueOf(rejectAlarm.getCount());

        val runTimeoutAlarm = getAlarmInfo(dtpName, RUN_TIMEOUT.getValue());
        String runTimeoutCount = runTimeoutAlarm == null ? UNKNOWN : String.valueOf(runTimeoutAlarm.getCount());

        val queueTimeoutAlarm = getAlarmInfo(dtpName, QUEUE_TIMEOUT.getValue());
        String queueTimeoutCount = queueTimeoutAlarm == null ? UNKNOWN : String.valueOf(queueTimeoutAlarm.getCount());

        return new ImmutableTriple<>(rejectCount, runTimeoutCount, queueTimeoutCount);
    }

    private static String buildKey(String dtpName, String notifyType) {
        return dtpName + ":" + notifyType;
    }
}
