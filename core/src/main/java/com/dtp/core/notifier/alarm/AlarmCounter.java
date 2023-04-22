package com.dtp.core.notifier.alarm;

import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.entity.AlarmInfo;
import com.dtp.core.support.ExecutorAdapter;
import com.dtp.core.thread.DtpExecutor;
import lombok.val;
import lombok.var;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.dtp.common.constant.DynamicTpConst.UNKNOWN;
import static com.dtp.common.em.NotifyItemEnum.QUEUE_TIMEOUT;
import static com.dtp.common.em.NotifyItemEnum.REJECT;
import static com.dtp.common.em.NotifyItemEnum.RUN_TIMEOUT;

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
