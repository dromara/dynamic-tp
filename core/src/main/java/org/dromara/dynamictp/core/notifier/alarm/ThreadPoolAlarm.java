package org.dromara.dynamictp.core.notifier.alarm;

import java.util.Optional;

/**
 * alarm interface
 *
 * @author kyao
 * @since 1.1.4
 */
public interface ThreadPoolAlarm {

    /**
     * get ThirdPartTpAlarmHelper
     *
     * @return ThirdPartTpAlarmHelper
     */
    ThreadPoolAlarmHelper getThirdPartTpAlarmHelper();

    /**
     * enhance execute method
     *
     * @param r
     */
    default void executeAlarmEnhance(Runnable r) {
        Optional.ofNullable(getThirdPartTpAlarmHelper()).ifPresent(alarmHelper ->
                alarmHelper.startQueueTimeoutTask(r)
        );
    }

    /**
     * enhance beforeExecute method
     *
     * @param t
     * @param r
     */
    default void beforeExecuteAlarmEnhance(Thread t, Runnable r) {
        Optional.ofNullable(getThirdPartTpAlarmHelper()).ifPresent(alarmHelper -> {
            alarmHelper.cancelQueueTimeoutTask(r);
            alarmHelper.startRunTimeoutTask(t, r);
        });
    }

    /**
     * enhance afterExecute method
     *
     * @param r
     * @param t
     */
    default void afterExecuteAlarmEnhance(Runnable r, Throwable t) {
        Optional.ofNullable(getThirdPartTpAlarmHelper()).ifPresent(alarmHelper ->
                alarmHelper.cancelRunTimeoutTask(r)
        );
    }

}
