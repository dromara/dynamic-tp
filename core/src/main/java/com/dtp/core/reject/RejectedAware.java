package com.dtp.core.reject;

import com.dtp.core.notify.alarm.AlarmManager;
import com.dtp.core.thread.DtpExecutor;

import java.util.concurrent.ThreadPoolExecutor;

import static com.dtp.common.em.NotifyTypeEnum.REJECT;

/**
 * RejectedAware related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public interface RejectedAware {

    /**
     * Do sth before reject.
     * @param executor ThreadPoolExecutor instance
     */
    default void beforeReject(ThreadPoolExecutor executor) {
        if (executor instanceof DtpExecutor) {
            DtpExecutor dtpExecutor = (DtpExecutor) executor;
            dtpExecutor.incRejectCount(1);
            Runnable runnable = () -> AlarmManager.doAlarm(dtpExecutor, REJECT);
            AlarmManager.triggerAlarm(dtpExecutor.getThreadPoolName(), REJECT.getValue(), runnable);
        }
    }
}
